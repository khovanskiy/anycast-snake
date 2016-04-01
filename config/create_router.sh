#!/bin/bash
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

if [ "$#" -ne 4 ]; then
    echo "${RED}Ошибка: неверное количество параметров${NC}"
    echo "Usage: sudo ./create_router.sh ИНТЕРФЕЙС_РОУТЕРА НОМЕР_РОУТЕРА КОЛИЧЕСТВО_РОУТЕРОВ СПИСОК_VLAN"
    exit 1
fi

#vlans=(11 12 13 14);
vlans=();

for v in $4; 
	do vlans+=(${v}); 
done;
echo ${newarray[*]}

vlan_count=${#vlans[*]};

echo "vlans count:" "${vlan_count}" 
echo "vlan list:" "${vlans[*]}"

router_interface=$1
router_number=$2
total_number=$3

if [ ${vlan_count} -ne ${total_number} ]; then
    echo "${RED}Ошибка: количество vlan должно быть равно количеству роутеров${NC}"
    exit 1
fi

#next_number=$(( (router_number - 1 + 1) % total_number + 1 ))
#prev_number=$(( (router_number - 1 - 1 + total_number) % total_number + 1 ))

prefix4="192.168"

echo "${YELLOW}##### Проверяем окружение #####${NC}"
echo "${GREEN}### Включаем IPv6 Forwarding ###${NC}"
echo 1 > /proc/sys/net/ipv6/conf/all/forwarding
# sysctl net.ipv6.conf.eth2.accept_ra=2
echo "${GREEN}### Устанавливаем ПО ###${NC}"
#sudo apt-get install vlan quagga bridge-utils isc-dhcp-server

echo "${GREEN}### Проверяем 8021q ###${NC}"
sudo modprobe 8021q
lsmod | grep 8021q

echo "${YELLOW}##### Настройки #####${NC}"
echo "${BLUE}Интерфейс     =${NC} ${router_interface}"
echo "${BLUE}Номер роутера =${NC} ${router_number}"

vlan_create() {
	echo "${GREEN}### Создаем VLAN ${1} ###${NC}"
	vconfig add $router_interface $1
}

vlan_up() {
	echo "${YELLOW}##### Настройка VLAN ${2} #####${NC}"
	current_vlan=$2;
	address4=$prefix4.$current_vlan.$1
	address6=fc00:192:168:$current_vlan::$1
	echo "${GREEN}### Присваиваем интерфейсу VLAN ${2} IPv4 адрес ${address4}/24 ###${NC}"
	ifconfig $router_interface.$current_vlan $address4/24 up
	sleep 0.5
	echo "${GREEN}### Присваиваем интерфейсу VLAN ${2} IPv6 адрес ${address6}/64 ###${NC}"
	ifconfig $router_interface.$current_vlan inet6 add $address6/64
	echo "${GREEN}### Проверяем VLAN ${2} ###${NC}"
	ifconfig -a | grep $router_interface.$current_vlan -A 5
}

echo "${YELLOW}##### Создаем VLAN-ы для подсетей #####${NC}"

for vlan in ${vlans[*]}
do
	vlan_create $vlan
done
vlan_up 1 $router_number$router_number
#next_vlan=$router_number$next_number
#vlan_create $next_vlan

#prev_vlan=$prev_number$router_number
#vlan_create $prev_vlan

#client_vlan=$router_number$router_number
#vlan_create $client_vlan

#vlan_up $router_number $next_vlan
#vlan_up $router_number $prev_vlan
#vlan_up 1 $client_vlan

# fc00:0000:0000:0000:0000:0000:0000:0001

echo "${YELLOW}##### Настройка /etc/quagga/daemons #####${NC}"
daemons_config() {
	echo "zebra=yes"
	echo "bgpd=no"
	echo "ospfd=no"
	echo "ospf6d=yes"
	echo "ripd=no"
	echo "ripngd=no"
	echo "isisd=no"
	echo "babeld=no"
}
daemons_config > /etc/quagga/daemons

echo "${YELLOW}##### Настройка /etc/quagga/zebra.conf #####${NC}"
zebra_config() {
	echo "hostname Router${router_number}"
	# Добавляем пароль: zebra
	echo "password zebra"
	echo "enable password zebra"
	# Включаем логгирование в /var/log/quagga/zebra.log
	echo "log file /var/log/quagga/zebra.log"
	echo "ip forwarding"

	echo "interface ${router_interface}.${current_vlan}"
	# https://www.sixxs.net/wiki/IPv6_tinc_routing_example#Stateless_autoconfiguration_of_the_LAN
	# http://www.nongnu.org/quagga/docs/docs-multi/Router-Advertisement.html
	# Send router advertisment messages
	echo " no ipv6 nd suppress-ra"
    echo " ipv6 address fc00:192:168:${current_vlan}::1/64"
    # Configuring the IPv6 prefix to include in router advertisements
    echo " ipv6 nd prefix fc00:192:168:${current_vlan}:1::/64"
    # The maximum time allowed between sending unsolicited multicast router advertisements from the interface, in seconds
    echo " ipv6 nd ra-interval 10"

	echo "line vty"
}
zebra_config > /etc/quagga/zebra.conf

echo "${YELLOW}##### Настройка /etc/quagga/ospf6d.conf #####${NC}"
ospf6d_config() {
	echo "hostname Router${router_number}"
	echo "password zebra"
	echo "enable password zebra"
	echo "service advanced-vty"
	# Включаем логгирование в /var/log/quagga/ospf6d.log
	echo "log file /var/log/quagga/ospf6d.log"
	echo "!"
	echo "router ospf6"
	echo " router-id ${router_number}.${router_number}.${router_number}.${router_number}"
	for vlan in ${vlans[*]}
	do
		echo " interface ${router_interface}.${vlan} area 0.0.0.0"
	done
	echo "!"
	for vlan in ${vlans[*]}
	do
		echo "interface ${router_interface}.${vlan}"
		echo " ipv6 ospf6 hello-interval 5"
		echo " ipv6 ospf6 dead-interval 10"
	done
}
ospf6d_config > /etc/quagga/ospf6d.conf

echo "${YELLOW}##### Меняем права и владельцев файлов настроек Quagga #####${NC}"
chown quagga.quaggavty /etc/quagga/*.conf
chmod 640 /etc/quagga/*.conf

echo "${YELLOW}##### Делаем рестарт сервиса Quagga #####${NC}"
/etc/init.d/quagga restart



