class phalangeList:
    phalangelist = []


    def refill(self,plist):
        templist = []
        appflag = 1
        for i in range(len(plist)):
                for j in range(len(self.phalangelist)):
                        if plist[i] == self.phalangelist[j].fileID:
                                templist.append(self.phalangelist[j])
        for i in range(len(plist)):
                for j in range(len(templist)):
                        if plist[i] == templist[j].fileID:
                                appflag = 0
                                break
                        else:
                                appflag = 1
                if appflag == 1:
                        templist.append(phalange(plist[i],None))
                        appflag = 0
        temp = set(self.phalangelist) - set(templist)
        temp1 = list(temp)
        temp2 = []
        del self.phalangelist[:]
        self.phalangelist = templist
        for i in range(len(temp1)):
            temp2.append(temp1[i].scannerID)
        return temp2


    
    def appendPL(self,fileID,scannerID):
        if self.returnfileID(scannerID) != -1 and self.returnscannerID(fileID) != -1:
            print 'entry already exists'
            return -1
        elif self.returnfileID(scannerID) != -1:
            raise PhalangeError('Enrollment already exists')
        elif self.returnscannerID(fileID) != -1:
            raise PhalangeError('file exists under different scannerID')
        else:
            self.phalangelist.append(phalange(fileID,scannerID))
            #print 'added' + fileID + 'as' + scannerID
        return 1

    def returnscannerID(self,fileID):
        for i in range(len(self.phalangelist)):
            if self.phalangelist[i].fileID == fileID:
                return self.phalangelist[i].scannerID
        return -1
    
    def returnfileID(self,scannerID):
        for i in range(len(self.phalangelist)):
            if self.phalangelist[i].scannerID == scannerID and \
               self.phalangelist[i].scannerID != None:
                return self.phalangelist[i].fileID
        return -1
    
    def returnfileID_index(self,fileID):
        for i in range(len(self.phalangelist)):
            if self.phalangelist[i].fileID == fileID:
                return i
        return -1

    def returnscannerID_index(self,scannerID):
        for i in range(len(self.phalangelist)):
            if self.phalangelist[i].scannerID == scannerID:
                return i
        return -1

    def delete_fileID(self,fileID):
        for i in range(len(self.phalangelist)):
            if self.phalangelist[i].fileID == fileID:
                del(self.phalangelist[i])
                return 1
        return -1

    def assign_scannerID(self,fileID,scannerID):
        index = self.returnfileID_index(fileID)
        if index == -1:
            return -1
        else:
            self.phalangelist[index].scannerID = scannerID
            return 1
    def printall(self):
        for i in range(len(self.phalangelist)):
            print '[' + str(self.phalangelist[i].fileID) + ', ' + \
                  str(self.phalangelist[i].scannerID) + ']'

    def printID(self):
        for i in range(len(self.phalangelist)):
            print self.phalangelist[i].fileID
            
    def printEnrollment(self):
        for i in range(len(self.phalangelist)):
            print self.phalangelist[i].scannerID


class phalange:
    def __init__(self, fileID, scannerID):
        self.fileID = fileID
        self.scannerID = scannerID

class PhalangeError(Exception):
    def __init__(self,msg):
        self.msg = msg

'''
poo = phalangeList()
poo.appendPL(111,1)
poo.appendPL(222,2)
poo.appendPL(333,3)
poo.appendPL(444,4)
poo.appendPL(555,5)
poo.appendPL(666,6)
poo.appendPL(777,7)
poo.appendPL(888,8)
'''
