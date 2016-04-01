if [ "$#" -ne 2 ]; then
    echo "${RED}Ошибка: неверное количество параметров${NC}"
    echo "Usage: sudo ./create_router.sh ИНТЕРФЕЙС_СЕРВЕРА НОМЕР_СЕРВЕРА"
    exit 1
fi
server_interface=$1
server_number=$2
current_vlan=$server_number$server_number
ifconfig $server_interface.$current_vlan inet6 del fc00::123