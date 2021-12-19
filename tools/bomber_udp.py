import os
import sys
import time
import socket
import threading

def receiveUDP():
    pass
 
def sendUDP():
    pass
 
localIP     = "192.168.0.108"
localPort   = 55555
bufferSize  = 20

 
msgFromServer       = "Hello UDP Client"
bytesToSend         = str.encode(msgFromServer)
id_distribution_packet  = b"\x00\x03\x01\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"
sync_packet  =            b"\x00\x01\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"

# Create a datagram socket
UDPServerSocket = socket.socket(family=socket.AF_INET, type=socket.SOCK_DGRAM)

 

# Bind to address and ip
print("UDP server binding")
UDPServerSocket.bind((localIP, localPort))




print("UDP server up and listening on Port %s %d"%(localIP, localPort))
#x = threading.Thread(target=sendUDP, args=())
#x.start()
try:
    bytesAddressPair = UDPServerSocket.recvfrom(bufferSize)
    message = bytesAddressPair[0]
    address = bytesAddressPair[1]
    clientIP  = "Client IP Address:{}".format(address)
    UDPServerSocket.sendto(id_distribution_packet, address)
except Exception as e:
    print(e)
    os.system("PAUSE") 

start = time.time()
idx = 0


# echo loop
try:
    while(True):
            

        bytesAddressPair = UDPServerSocket.recvfrom(bufferSize)
      
        clientMsg = "Message from Client:{}".format(message)
        clientIP  = "Client IP Address:{}".format(address)
        print(str(message[2])+") " +str((time.time()-start)*1000)+"ms "+clientIP)
        message = bytesAddressPair[0]
        address = bytesAddressPair[1]


        # Sending a reply to client
        start = time.time()
        UDPServerSocket.sendto(message, address)
except Exception as e:
    print(e)
    os.system("PAUSE") 
    
   