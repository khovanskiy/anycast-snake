if [ "$#" -ne 2 ]; then
    echo "${RED}Ошибка: неверное количество параметров${NC}"
    echo "Usage: sudo ./create_router.sh ИНТЕРФЕЙС_СЕРВЕРА НОМЕР_СЕРВЕРА"
    exit 1
fi
server_interface=$1
server_number=$2
current_vlan=$server_number$server_number
anycast_ip=fc00::123
anycast_port=1234
ifconfig $server_interface.$current_vlan inet6 add $anycast_ip
sleep 1
port=$server_number$server_number$server_number$server_number
java -jar server/target/server.jar server-$server_number fc00:192:168:$current_vlan::1 $port $anycast_ip $anycast_port