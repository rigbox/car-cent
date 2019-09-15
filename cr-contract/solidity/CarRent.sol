pragma solidity ^0.5.2;

contract CarRent {
    
    struct Vehicle {
        address vAddress;
        address vManager;
        address uAddress;
        string vId;
        string vColor;
        string vType;
        VehicleStatus vStatus;
        bool isVaild;
    }
    
    enum VehicleStatus { Unused, Ordered, Opened, Using }
    
    
    //contract owner
    address owner;
    
    //Arrays for the address of manager
    address[] managers;
    
    //Arrays for the address of vehicle 
    address[] vehicles;
    
    //mapping for the Vehicle address to the Vehicle
    mapping (address => Vehicle) vMap;
    
    //The Add Vehicle Event when the Car Wallet was register
    event AddVehicle(address _vAddress, string _vId);
    //The Delete Vehicle Event when the Car Wallet was Delete
    event DelVehicle(address _vAddress, string _vId);
    //The OrderedVehicle Event when the User ordered the Vehicle
    event OrderedVehicle(address _uAddress, address _vAddress);
    //The CancelVehicle Event when the User cancel book the Vehicle
    event CancelVehicle(address _uAddress, address _vAddress);
    //The OpenedVehicle Event when the User request to open the door of the Vehicle
    event OpenedVehicle(address _uAddress, address _vAddress);
    //The StartedVehicle Event when the Vehicle has opened the door of the Vehicle
    event StartedVehicle(address _uAddress, address _vAddress);
    
    constructor() public {
        owner = msg.sender;
        managers.push(owner);
    }
    
    function approval(address manager) public returns (bool) {
        require(manager != msg.sender);
        require(owner == msg.sender,"Role is error!");
        managers.push(manager);
        return true;
    }
    
    
    modifier verifyVid(address _vAddress, string memory _vid) {
        require(!vMap[_vAddress].isVaild);
        require(vMap[_vAddress].vAddress == address(0));
        require(keccak256(bytes(_vid)) != keccak256(bytes(vMap[_vAddress].vId)));
        _;
    }
    
    modifier onlyManager() {
        for(uint i=0; i < managers.length; i++) {
            if(msg.sender == managers[i]) {
                _;
                break;
            }
        }
    }
    
    modifier isVehicle(address _vAddress) {
        require(vMap[_vAddress].vAddress != address(0));
        require(vMap[_vAddress].isVaild);
        _;
    }
    
    function addVehicle(address _vAddress, string memory _vId, string memory _vColor, string memory _vType) 
        public verifyVid(_vAddress, _vId) onlyManager returns (bool) {
        
        Vehicle memory newV = Vehicle({
           vAddress : _vAddress,
           vManager : msg.sender,
           uAddress : _vAddress,
           vId : _vId,
           vColor : _vColor,
           vType : _vType,
           vStatus : VehicleStatus.Unused,
           isVaild : true
        });
        
        vehicles.push(_vAddress);
        vMap[_vAddress] = newV;
        emit AddVehicle(_vAddress,_vId);
        return true;
    }
    
    function delVehicle(address _vAddress) public isVehicle(_vAddress) onlyManager returns (bool) {
        Vehicle memory v = vMap[_vAddress];
        require(v.vStatus ==  VehicleStatus.Unused);
        for(uint i=0; i < vehicles.length; i++) {
            if(_vAddress == vehicles[i]){
                delete vehicles[i];
                delete vMap[_vAddress];
                emit DelVehicle(v.vAddress,v.vId);
                break;
            }
        }
        return true;
    }
    
    function getUnusedVehicle(uint _index) public view 
        returns(address _vAddress, string memory _vId, string memory _vColor, string memory _vType, VehicleStatus _vStatus) {
        require(vehicles.length>0);
        require(_index < vehicles.length);
        uint num = _index;
        for(uint i=_index; i < vehicles.length; i++) {
            if(vMap[vehicles[i]].isVaild == false) {
                continue;
            }
            if(vMap[vehicles[i]].vStatus==VehicleStatus.Unused) {
                if(_index==num) {
                    Vehicle storage v = vMap[vehicles[i]];
                    _vAddress = v.vAddress;
                    _vId = v.vId;
                    _vColor = v.vColor;
                    _vType = v.vType;
                    _vStatus = v.vStatus;
                    break;
                }
                num ++;
            }
        }
    }
    
    
    function getVehicle(address _vAddress) public view isVehicle(_vAddress)
        returns(string memory _vId, string memory _vColor, string memory _vType, VehicleStatus _vStatus) {
        Vehicle storage v = vMap[_vAddress];
        _vId = v.vId;
        _vColor = v.vColor;
        _vType = v.vType;
        _vStatus = v.vStatus;
    }
    
    //Order the Vehicle for User Wallet
    function orderVehicle(address _vAddress) public returns(bool){
        Vehicle storage v = vMap[_vAddress];
        require(v.vStatus == VehicleStatus.Unused);
        v.uAddress = msg.sender;
        v.vStatus = VehicleStatus.Ordered;
        emit OrderedVehicle(msg.sender, _vAddress);
        return true;
    }
    
    
    //Cancel Order the Vehicle for User Wallet
    function cancelVehicle(address _vAddress) public returns(bool){
        Vehicle storage v = vMap[_vAddress];
        require(v.vStatus == VehicleStatus.Ordered);
        require(v.uAddress == msg.sender);
        v.uAddress = msg.sender;
        v.vStatus = VehicleStatus.Unused;
        emit CancelVehicle(msg.sender, _vAddress);
        return true;
    }
    
    //Open the Vehicle for User Wallet
    function openVehicle(address _vAddress) public returns(bool) {
        Vehicle storage v = vMap[_vAddress];
        require(v.vStatus == VehicleStatus.Ordered);
        require(v.uAddress == msg.sender);
        v.uAddress = msg.sender;
        v.vStatus = VehicleStatus.Opened;
        emit OpenedVehicle(msg.sender, _vAddress);
        return true;
    }
    
    // Start to using the Vehicle for Vehicle Wallet
    function startVehicle(address _vAddress) public returns(bool) {
        require(_vAddress == msg.sender);
        Vehicle storage v = vMap[msg.sender];
        require(v.vStatus == VehicleStatus.Opened);
        v.vStatus = VehicleStatus.Using;
        emit StartedVehicle(v.uAddress, v.vAddress);
        return true;
    }
}