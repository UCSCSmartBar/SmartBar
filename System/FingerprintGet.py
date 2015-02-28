from paramiko import SSHClient
from scp import SCPClient
import time
import os
import shutil


def fpget_init():
    if not os.path.exists('/tmp/Fingers'):
        os.mkdir('/tmp/Fingers')
        print 'directory created'
    else:
        print 'directory exists'


def get_files(string):
    sList = string.split(',')
    print sList
    ctime = time.time()
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
            scp.get(fizzle,'/tmp/Fingers')
        except:
            print(fizzle + ' not found')
            return 0
    ctime = time.time() - ctime
    print 'finished in ' + str(ctime) + 's'
    return 1

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

