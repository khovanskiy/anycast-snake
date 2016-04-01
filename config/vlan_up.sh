#!/bin/bash
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

if [ "$#" -ne 3 ]; then
    echo "${RED}Ошибка: неверное количество параметров${NC}"
    echo "Usage: sudo ./vlan_up.sh ИНТЕРФЕЙС_РОУТЕРА НОМЕР_РОУТЕРА НОМЕР_VLAN"
    exit 1
fi

router_interface=$1
router_number=$2
current_vlan=$3

prefix4="192.168"
address4=$prefix4.$current_vlan.$router_number
address6=fc00:192:168:$current_vlan::$router_number

echo "${YELLOW}##### Настройка VLAN ${current_vlan} #####${NC}"
echo "${GREEN}### Присваиваем интерфейсу VLAN ${current_vlan} IPv4 адрес ${address4}/24 ###${NC}"
ifconfig $router_interface.$current_vlan $address4/24 up
sleep 0.5
echo "${GREEN}### Присваиваем интерфейсу VLAN ${current_vlan} IPv6 адрес ${address6}/64 ###${NC}"
ifconfig $router_interface.$current_vlan inet6 add $address6/64
echo "${GREEN}### Проверяем VLAN ${current_vlan} ###${NC}"
ifconfig -a | grep $router_interface.$current_vlan -A 5