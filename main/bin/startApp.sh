#!/bin/bash
OLD_APP_JAR=aml-web.jar
# APP_JAR=test_aml.jar
APP_NAME=aml-web

JAVA_OPTION=
SHOW_LOG=$2

checkpid(){
    pid=`ps -ef |grep $OLD_APP_JAR |grep -v grep |awk '{print  $2}'`
}

stop()
{
	kill -9 `ps -ef | grep $OLD_APP_JAR |grep -v grep| awk '{print  $2}'`
}

status(){
   checkpid
   if [ ! -n "$pid" ]; then
     echo "$APP_NAME not runing"
   else
     echo "$APP_NAME runing PID: $pid"
   fi
}


start()
{
  basepath=$(cd `dirname $0`; pwd)
  pwd

 checkpid
  if [  -n "$pid" ]; then
  	 echo "kill $pid"
     stop
  fi


  if [ ! -f $OLD_APP_JAR ];then
  		 echo "$OLD_APP_JAR 文件不存在..$PATH"
	else
	  if [ ! -f $APP_NAME.nohup ];then
	  	 if [ ! -d logs ]; then
	     	mkdir logs
	     fi
	  	 mv ${APP_NAME}.nohup  ./logs/${APP_NAME}$(date "+%Y%m%d%H%M%S").nohup
	  fi
	  nohup java $JAVA_OPTION -jar $OLD_APP_JAR > ${APP_NAME}.nohup 2>&1 &
	 if [ "$SHOW_LOG" != "nolog" ]; then
            sleep 1
	    tail -f  ${APP_NAME}.nohup
	fi
 fi
}


if [[ $1 == "stop" ]]
	then
	echo "stop $APP_NAME"
	stop
	
elif [[ $1 == "start" ]]
	then
	echo "start $APP_NAME"
	start
	
elif [[ $1 == "restart" ]]
	then
	 
	echo "restart $APP_NAME"
	start
elif [[ $1 == "status" ]]
	 then
	  status
else
	echo -e "Use start, stop, restart,status ........."
fi 
