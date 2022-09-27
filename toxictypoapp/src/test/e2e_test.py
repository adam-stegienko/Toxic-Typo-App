#!/bin/python
# Accepts 3 parameters: <host:port> <test-file> <wait>
# host - defines what server tests will be run against. Defualt = localhost:8080
# test-file - defines file with list of tests. Default = sanity
# wait - wait time in seconds before starting to execute tests. Default = 0

import json
import random
import requests
import sys
import time

def hms_string(sec_elapsed):
    h = int(sec_elapsed / (60 * 60))
    m = int((sec_elapsed % (60 * 60)) / 60)
    s = sec_elapsed % 60.
    return "{}:{:>02}:{:>05.2f}".format(h, m, s)

def suggest(w,srv):
    r = requests.get("http://%s/api/suggest/%s"%(srv,w))
    o = json.loads(r.text)
    return o["suggestion"]

def test(expect,send,srv):
    suggestion=suggest(send,srv)

    if expect=="Nothing":
        if suggestion is None or suggestion=="None" or suggestion==None:
            return True
        else:
            print "@{}@".format(suggestion)

    success = (suggestion==expect)
    if not success: print "Failure: Expected %s and got %s"%(expect,suggestion)
    return success

def getArg(args,name,i,d):
    ret = d
    if len(args)>i:
      ret = args[i]
    print "%s is set to %s"%(name,ret)
    return ret

start_time = time.time()

server=getArg(sys.argv,"Server",1,"localhost:8080")
testFile=getArg(sys.argv,"Test level",2,"sanity")
wt=int(getArg(sys.argv,"Wait time",3,"0"))

if wt>0:
  print "Waiting %d seconds before starting to send test messages..."%wt
  time.sleep(wt)

i=0
b=0
ret=0
f=open(testFile,"r")
for line in f:
    i+=1
    if (i%50)==0:
        print "%d tests and counting..."%i
    words = line.strip().split("->")
    if not test(words[1],words[0],server):
        ret=1
        b+=1


end_time = time.time()
print "{} tests performed in {}.".format(i,hms_string(end_time - start_time))
if b>0:
    print "{} failures!".format(b)
exit(ret)
