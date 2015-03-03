from paramiko import SSHClient
from scp import SCPClient
import time
import os
import shutil
import phalange

queue = phalange.phalangeList()

def fpget_init():
    if not os.path.exists('/tmp/Fingers'):
        os.mkdir('/tmp/Fingers')
        print 'directory created'
    else:
        print 'directory exists'

def update_queue(string):
    sList = []
    ctime = time.time()
    clear_files()
    sList = get_files(string)
    del sList[0]
    sList = map(int, sList)
    queue.refill(sList)
    queue.printall()
    ctime = time.time() - ctime
    print 'finished in ' + str(ctime) + 's'

def get_files(string):
    sList = string.split(',')
    print sList
    ssh = SSHClient()
    ssh.load_system_host_keys()
    print 'connecting'
    ssh.connect('unix.ucsc.edu',22,'masierra','Coralsandus24482')
    print 'getting transport'
    scp = SCPClient(ssh.get_transport())
    print 'getting files'
    for i in range(1,len(sList)):
        fizzle = 'SmartBar/' + sList[i]
        try:
            scp.get(fizzle,'/tmp.Fingers')
        except:
            print 'file not found: ' + fizzle
            return 0
    return sList

def clear_files():
    shutil.rmtree('/tmp/Fingers')
    fpget_init()

def add_files():
    ctime = time.time()
    ssh = SSHClient()
    ssh.load_system_host_keys()
    print 'connecting'
    ssh.connect('unix.ucsc.edu',22,'masierra','Coralsandus24482')
    print 'getting transport'
    scp = SCPClient(ssh.get_transport())
    
    scp.put('/tmp/Fingers/New', 'SmartBar/')
    ctime = time.time() - ctime
    print 'finished in ' + str(ctime) + 's'
