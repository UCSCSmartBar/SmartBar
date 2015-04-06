import time

# Class: PrintFilter
# Description: Only prints statements if message type is enables
# Author: Brendan Short
# Last Modified: 2/13/2015

# Data Log Codes:
# ^ : Self
# > : Recieved
# < : Sent

self_logcode = 0
input_logcode = 1
output_logcode = 2
log_code_array = ["^",">","<"]
class PrinterLogger():

    def __init__(self,system_info_enabled,debugging_info_enabled,warnings_enabled,error_messages_enabled,system_name,spacing_size):

        self.LogPath = "../Logs"
        self.Spacing = spacing_size
        self.Title = 1
        self.SubTitle = 2
        self.Standard = 0
        self.MessageIndicators = ["$","*","@","&"]
        self.SystemIndex = 0
        self.CommandIndex = 5
        self.DebugIndex = 1
        self.WarnIndex = 2
        self.ErrorIndex = 3
        self.WriteToAllLogs = 4
        self.MessagePrefix = [str(system_name)+" System : ",str(system_name)+" Debug : ",str(system_name)+" Warning : ", str(system_name)+" Error : "]
        self.Space = " "
        self.NoSpace = ""
        self.MessageBarIndent = 5
        self.MessageBarSize = 75
        self.SpaceAfterLastPrinted = 0


        self.CommandResults = ["Fail","Success"]
        self.MixType = ["Alcohol","Mixer"]


        self.DataLogFilePath = (self.LogPath+"/"+str(system_name)+"_Log_Data.sbl")
        self.TextLogFilePath = (self.LogPath+"/"+str(system_name)+"_Log_Text.sbl")

        self.LineSplittingCharacter = ","
        self.PacketSplittingCharacter = "@"

        
        if (self.Spacing == 0):
            self.SpaceBeforeTitle = self.NoSpace
            self.SpaceAfterTitle = self.NoSpace
            self.SpaceBeforeSubTitle = self.NoSpace
            self.SpaceAfterSubTitle = self.NoSpace
            self.SpaceBeforeStandard = self.NoSpace
            self.SpaceAfterStandard = self.NoSpace
        if (self.Spacing == 1):
            self.SpaceBeforeTitle = '\n'
            self.SpaceAfterTitle = '\n'
            self.SpaceBeforeSubTitle = self.NoSpace
            self.SpaceAfterSubTitle = self.NoSpace
            self.SpaceBeforeStandard = self.NoSpace
            self.SpaceAfterStandard = self.NoSpace
        if (self.Spacing == 2):
            self.SpaceBeforeTitle = '\n'
            self.SpaceAfterTitle = '\n'
            self.SpaceBeforeSubTitle = '\n'
            self.SpaceAfterSubTitle = '\n'
            self.SpaceBeforeStandard = self.NoSpace
            self.SpaceAfterStandard = self.NoSpace
        if (self.Spacing == 3):
            self.SpaceBeforeTitle = '\n'
            self.SpaceAfterTitle = '\n'
            self.SpaceBeforeSubTitle = '\n'
            self.SpaceAfterSubTitle = '\n'
            self.SpaceBeforeStandard = '\n'
            self.SpaceAfterStandard = '\n'

        self.CurrentMessageToLog = 'Empty'



        self.LocalTime = time.localtime(time.time())
        self.SystemInfoEnabled = system_info_enabled # system information on/off
        self.DebuggingEnabled = debugging_info_enabled # debugging messages on/off
        self.WarningsEnabled = warnings_enabled # display warnings on/off
        self.ErrorMessagesEnabled = error_messages_enabled

        self.DataLogEnabled = 1
        self.TextLogEnabled = 1

        
        self.LogStartingCode = "^SS"
        self.StartDataLogger()

            
    def System(self, system_message, message_type):     
        if (self.SystemInfoEnabled == 1):
            if (message_type > 0):
                self.TitleMessage(self.SystemIndex,message_type,system_message)
            else:
                if (self.SpaceAfterLastPrinted == 1):
                    print(system_message+self.SpaceAfterStandard)
                    self.CurrentMessageToLog = (system_message+self.SpaceAfterStandard)
                else:
                    print(self.SpaceBeforeStandard+system_message+self.SpaceAfterStandard)
                    self.CurrentMessageToLog = (system_message+self.SpaceAfterStandard)

                if (self.SpaceAfterStandard == '\n'):
                    self.SpaceAfterLastPrinted = 1
                else:
                    self.SpaceAfterLastPrinted = 0
        self.WriteToTextLog()
                    
    def Debug(self, debug_message,message_type):
        
        if (self.DebuggingEnabled == 1):
            
            if (message_type > 0):
                self.TitleMessage(self.DebugIndex, message_type,debug_message)
            else:
                
                if (self.SpaceAfterLastPrinted == 1):
                    print(self.MessageIndicators[self.DebugIndex]+self.MessagePrefix[self.DebugIndex]+debug_message+self.SpaceAfterStandard)
                else:
                    print(self.SpaceBeforeStandard+self.MessageIndicators[self.DebugIndex]+self.MessagePrefix[self.DebugIndex]+debug_message+self.SpaceAfterStandard)

                if (self.SpaceAfterStandard == '\n'):
                    self.SpaceAfterLastPrinted = 1
                else:
                    self.SpaceAfterLastPrinted = 0
        self.WriteToTextLog()
                    
    def Warn(self, warning_message, message_type):          
        if (self.WarningsEnabled == 1):
            if (message_type > 0):
                self.TitleMessage(self.WarnIndex, message_type,warning_message)
            else:
                if (self.SpaceAfterLastPrinted == 1):
                    print(self.MessageIndicators[self.WarnIndex]+self.MessagePrefix[self.WarnIndex]+warning_message+self.SpaceAfterStandard)
                    self.CurrentMessageToLog = (self.MessageIndicators[self.WarnIndex]+self.MessagePrefix[self.WarnIndex]+warning_message+self.SpaceAfterStandard)
                else:
                    print(self.SpaceBeforeStandard+self.MessageIndicators[self.WarnIndex]+self.MessagePrefix[self.WarnIndex]+warning_message+self.SpaceAfterStandard)
                    self.CurrentMessageToLog = (self.SpaceBeforeStandard+self.MessageIndicators[self.WarnIndex]+self.MessagePrefix[self.WarnIndex]+warning_message+self.SpaceAfterStandard)
                    
                if (self.SpaceAfterStandard == '\n'):
                    self.SpaceAfterLastPrinted = 1
                else:
                    self.SpaceAfterLastPrinted = 0
                    
                    
    def Error(self, error_message, message_type):          
        if (self.ErrorMessagesEnabled == 1):
            if (message_type > 0):
                self.TitleMessage(self.ErrorIndex,message_type,error_message)
            else:
                if (self.SpaceAfterLastPrinted == 1):
                    print(self.MessageIndicators[self.ErrorIndex]+self.MessagePrefix[self.ErrorIndex]+error_message+self.SpaceAfterStandard)
                    self.CurrentMessageToLog = (self.MessageIndicators[self.ErrorIndex]+self.MessagePrefix[self.ErrorIndex]+error_message+self.SpaceAfterStandard)
                else:
                    print(self.SpaceBeforeStandard+self.MessageIndicators[self.ErrorIndex]+self.MessagePrefix[self.ErrorIndex]+error_message+self.SpaceAfterStandard)            
                    self.CurrentMessageToLog = (self.SpaceBeforeStandard+self.MessageIndicators[self.ErrorIndex]+self.MessagePrefix[self.ErrorIndex]+error_message+self.SpaceAfterStandard)
                    
                if (self.SpaceAfterStandard == '\n'):
                    self.SpaceAfterLastPrinted = 1
                else:
                    self.SpaceAfterLastPrinted = 0
        self.WriteToTextLog()
        
    def TitleMessage(self, filter_type, message_type,message_content):


        MessageLength = len(message_content) + len(self.MessagePrefix[filter_type]) + self.MessageBarIndent + 3*len(self.Space)                                                
        MessageBar = self.NoSpace.join([self.MessageIndicators[filter_type]] * self.MessageBarSize )  # state (open/closed) of all of the valves, initialized to 0 (closed)
        if (len(message_content) > (self.MessageBarSize - len(self.Space.join((self.NoSpace.join([self.MessageIndicators[filter_type]] * self.MessageBarIndent),self.MessagePrefix[filter_type]))))):
            Message = self.Space.join((self.NoSpace.join([self.MessageIndicators[filter_type]] * self.MessageBarIndent),
                                       self.MessagePrefix[filter_type],MessageBar[(int(MessageLength)-1-len(message_content)):],'\n',message_content))
        else:
            Message = self.Space.join((self.NoSpace.join([self.MessageIndicators[filter_type]] * self.MessageBarIndent),self.MessagePrefix[filter_type],message_content,MessageBar[int(MessageLength):]))
        if (message_type == self.Title):

            CurrentDate = time.time()
            TimeLogBar = (self.NoSpace.join([self.MessageIndicators[filter_type]] * self.MessageBarIndent)+' '+str(self.LocalTime[2])+
                          '/'+str(self.LocalTime[1])+'/'+str(self.LocalTime[0])+' @ '+str(self.LocalTime[3])+':'+
                          str(self.LocalTime[4])+':'+str(self.LocalTime[5])+' ')

            TimeLogBarLength = len(TimeLogBar)
            TimeLogBar = TimeLogBar+MessageBar[TimeLogBarLength:]
            if (self.SpaceAfterLastPrinted == 1):
                print(MessageBar+'\n'+TimeLogBar)
                self.CurrentMessageToLog = MessageBar+'\n'+TimeLogBar
            else:
                print(self.SpaceBeforeTitle+MessageBar+'\n'+TimeLogBar)
                self.CurrentMessageToLog = (self.SpaceBeforeTitle+MessageBar+'\n'+TimeLogBar)
                                    
            print(Message)
            print(MessageBar +self.SpaceAfterTitle)
            self.CurrentMessageToLog = self.CurrentMessageToLog+'\n'+str(Message)+'\n'+(MessageBar +self.SpaceAfterTitle)
            if (self.SpaceAfterTitle == '\n'):
                self.SpaceAfterLastPrinted = 1
            else:
                self.SpaceAfterLastPrinted = 0   

        else:

            if (self.SpaceAfterLastPrinted == 1):
                print(Message+self.SpaceAfterSubTitle)
                self.CurrentMessageToLog = (Message+self.SpaceAfterSubTitle)
            else:
                print(self.SpaceBeforeSubTitle+Message +self.SpaceAfterSubTitle)
                self.CurrentMessageToLog = (self.SpaceBeforeSubTitle+Message +self.SpaceAfterSubTitle)

            if (self.SpaceAfterSubTitle == '\n'):
                self.SpaceAfterLastPrinted = 1
            else: 
                self.SpaceAfterLastPrinted = 0


    def StartDataLogger(self):

        StartUpTimeCode = self.LineSplittingCharacter.join(([str(self.LocalTime[0]),str(self.LocalTime[1]),str(self.LocalTime[2]),
                                                                           str(self.LocalTime[3]),str(self.LocalTime[4]),str(self.LocalTime[5])]))        
        LogStartDateCode = self.PacketSplittingCharacter.join([self.LogStartingCode,(StartUpTimeCode+'\n')])
        StartUpDateString = ('Start Up #  - '+str(self.LocalTime[2])+'/'+str(self.LocalTime[1])+'/'+str(self.LocalTime[0])+' at '+
                                                                           str(self.LocalTime[3])+':'+str(self.LocalTime[4])+':'+str(self.LocalTime[5]))
        try:
            self.TextFile = open(self.TextLogFilePath,'a')
            self.TextFile.close()
        except:
            self.TextFile = open(self.TextLogFilePath,'w')
            self.TextFile.close()
        try:
            self.DataFile = open(self.DataLogFilePath,'a')
            self.DataFile.close()
        except:
            self.DataFile = open(self.DataLogFilePath,'w')
            self.DataFile.close()
        self.System(StartUpDateString,self.Title)
        self.WriteToDataLog(LogStartDateCode,self_logcode)
                 

    def WriteToTextLog(self):
        
        if (self.TextLogEnabled == 1):
            self.TextFile = open(self.TextLogFilePath,'a')
            self.TextFile.write((self.CurrentMessageToLog+'\n'))
            self.DataFile.close()
        
    def WriteToDataLog(self,message,code_type):

        if (self.DataLogEnabled == 1):
            self.DataFile = open(self.DataLogFilePath,'a')
            self.DataFile.write(log_code_array[code_type]+message)
            self.DataFile.close()

  
