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
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# Bind to address and ip

print("TCP server binding")
s.bind((localIP, localPort))

print("TCP server listening")
s.listen(1)
conn, addr = s.accept()

conn.send(id_distribution_packet)  # send id
start = 0
# echo loop
try:
    while(True):
         
        message = conn.recv(bufferSize)
   
        #print(") " +str((time.time()-start)*1000)+"ms ")
        print(str(message[2])+") " +str((time.time()-start)*1000)+"ms ")

        # Sending a reply to client
        start = time.time()
        conn.send(message)  # echo
except Exception as e:
    print(e)
    os.system("PAUSE") 
    
   