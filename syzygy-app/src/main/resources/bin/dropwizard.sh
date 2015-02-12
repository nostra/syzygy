#!/usr/bin/env bash  
#
# Startup script for dropwizard, leaning heavily on jetty startup script
# Ref: http://www.eclipse.org/jetty/

usage()
{
    echo "Usage: ${0##*/} [-d] {start|stop|run|restart|check|supervise} [ CONFIGS ... ] "
    exit 1
}

[ $# -gt 0 ] || usage


##################################################
# Some utility functions
##################################################
findDirectory()
{
  local L OP=$1
  shift
  for L in "$@"; do
    [ "$OP" "$L" ] || continue 
    printf %s "$L"
    break
  done 
}

running()
{
  local PID=$(cat "$1" 2>/dev/null) || return 1
  kill -0 "$PID" 2>/dev/null
}

readConfig()
{
  (( DEBUG )) && echo "Reading $1.."
  source "$1"
}



##################################################
# Get the action & configs
##################################################
CONFIGS=()
NO_START=0
DEBUG=0

while [[ $1 = -* ]]; do
  case $1 in
    -d) DEBUG=1 ;;
  esac
  shift
done
ACTION=$1
shift


##################################################
# Set tmp if not already set.
##################################################
TMPDIR=${TMPDIR:-/tmp}


##################################################
# Try to determine DROPWIZARD_HOME if not set
##################################################
if [ -z "$DROPWIZARD_HOME" ] 
then
  DROPWIZARD_SH=$0
  case "$DROPWIZARD_SH" in
    /*)   ;;
    ./*)  ;;
    *)    DROPWIZARD_SH=./$DROPWIZARD_SH ;;
  esac
  DROPWIZARD_HOME=${DROPWIZARD_SH%/*/*}

  if [ ! -f "${DROPWIZARD_SH%/*/*}/$DROPWIZARD_INSTALL_TRACE_FILE" ]
  then 
    DROPWIZARD_HOME=
  fi
fi



##################################################
# No DROPWIZARD_HOME yet? We're out of luck!
##################################################
if [ -z "$DROPWIZARD_HOME" ]; then
  echo "** ERROR: DROPWIZARD_HOME not set, you need to set it or install in a standard location" 
  exit 1
fi

cd "$DROPWIZARD_HOME"
DROPWIZARD_HOME=$PWD


##################################################
# Try to find this script's configuration file,
# but only if no configurations were given on the
# command line.
##################################################
if [ -z "$DROPWIZARD_CONF" ] 
then
  if [ -f /etc/dropwizard.conf ]
  then
    DROPWIZARD_CONF=/etc/dropwizard.conf
  elif [ -f "$DROPWIZARD_HOME/etc/dropwizard.conf" ]
  then
    DROPWIZARD_CONF=$DROPWIZARD_HOME/etc/dropwizard.conf
  fi
fi


#####################################################
# Find a location for the pid file
#####################################################
if [ -z "$DROPWIZARD_RUN" ] 
then
  DROPWIZARD_RUN=$(findDirectory -w /var/run /usr/var/run $DROPWIZARD_HOME /tmp)
fi

#####################################################
# Find a PID for the pid file
#####################################################
if [ -z "$DROPWIZARD_PID" ] 
then
  DROPWIZARD_PID="$DROPWIZARD_RUN/dropwizard.pid"
fi

##################################################
# Setup JAVA if unset
##################################################
if [ -z "$JAVA" ]
then
  JAVA=$(which java)
fi

if [ -z "$JAVA" ]
then
  echo "Cannot find a Java JDK. Please set either set JAVA or put java (>=1.7) in your PATH." 2>&2
  exit 1
fi

#####################################################
# Are we running on Windows? Could be, with Cygwin/NT.
#####################################################
case "`uname`" in
CYGWIN*) PATH_SEPARATOR=";";;
*) PATH_SEPARATOR=":";;
esac


#####################################################
# This is how the Dropwizard server will be started
#####################################################
DROPWIZARD_START=$DROPWIZARD_HOME/app/*service*.jar
[ ! -f "$DROPWIZARD_START" ] && DROPWIZARD_START=$DROPWIZARD_HOME/app/*.jar


RUN_ARGS=(${JAVA_OPTIONS[@]} -jar "$DROPWIZARD_START" $DROPWIZARD_ARGS "${CONFIGS[@]}")
RUN_CMD=("$JAVA" ${RUN_ARGS[@]} "server" "$DROPWIZARD_CONF")

#####################################################
# Comment these out after you're happy with what 
# the script is doing.
#####################################################
if (( DEBUG ))
then
  echo "DROPWIZARD_HOME     =  $DROPWIZARD_HOME"
  echo "DROPWIZARD_CONF     =  $DROPWIZARD_CONF"
  echo "DROPWIZARD_RUN      =  $DROPWIZARD_RUN"
  echo "DROPWIZARD_PID      =  $DROPWIZARD_PID"
  echo "DROPWIZARD_ARGS     =  $DROPWIZARD_ARGS"
  echo "CONFIGS        =  ${CONFIGS[*]}"
  echo "JAVA_OPTIONS   =  ${JAVA_OPTIONS[*]}"
  echo "JAVA           =  $JAVA"
  echo "RUN_CMD        =  ${RUN_CMD}"
fi

##################################################
# Do the action
##################################################
case "$ACTION" in
  start)
    echo -n "Starting Dropwizard: "

    if (( NO_START )); then 
      echo "Not starting dropwizard - NO_START=1";
      exit
    fi

    if type start-stop-daemon > /dev/null 2>&1 
    then
      unset CH_USER
      if [ -n "$DROPWIZARD_USER" ]
      then
        CH_USER="-c$DROPWIZARD_USER"
      fi
      if start-stop-daemon -S -p"$DROPWIZARD_PID" $CH_USER -d"$DROPWIZARD_HOME" -b -m -a "$JAVA" -- "${RUN_ARGS[@]}" --daemon
      then
        sleep 1
        if running "$DROPWIZARD_PID"
        then
          echo "OK"
        else
          echo "FAILED"
        fi
      fi

    else

      if [ -f "$DROPWIZARD_PID" ]
      then
        if running $DROPWIZARD_PID
        then
          echo "Already Running!"
          exit 1
        else
          # dead pid file - remove
          rm -f "$DROPWIZARD_PID"
        fi
      fi

      if [ "$DROPWIZARD_USER" ] 
      then
        touch "$DROPWIZARD_PID"
        chown "$DROPWIZARD_USER" "$DROPWIZARD_PID"
        # FIXME: Broken solution: wordsplitting, pathname expansion, arbitrary command execution, etc.
        su - "$DROPWIZARD_USER" -c "
          exec ${RUN_CMD[*]} --daemon &
          disown \$!
          echo \$! > '$DROPWIZARD_PID'"
      else
        "${RUN_CMD[@]}" &
        disown $!
        echo $! > "$DROPWIZARD_PID"
      fi

      echo "STARTED Dropwizard `date`" 
    fi

    ;;

  stop)
    echo -n "Stopping Dropwizard: "
    if type start-stop-daemon > /dev/null 2>&1; then
      start-stop-daemon -K -p"$DROPWIZARD_PID" -d"$DROPWIZARD_HOME" -a "$JAVA" -s HUP
      
      TIMEOUT=30
      while running "$DROPWIZARD_PID"; do
        if (( TIMEOUT-- == 0 )); then
          start-stop-daemon -K -p"$DROPWIZARD_PID" -d"$DROPWIZARD_HOME" -a "$JAVA" -s KILL
        fi

        sleep 1
      done

      rm -f "$DROPWIZARD_PID"
      echo OK
    else
      PID=$(cat "$DROPWIZARD_PID" 2>/dev/null)
      kill "$PID" 2>/dev/null
      
      TIMEOUT=30
      while running $DROPWIZARD_PID; do
        if (( TIMEOUT-- == 0 )); then
          kill -KILL "$PID" 2>/dev/null
        fi

        sleep 1
      done

      rm -f "$DROPWIZARD_PID"
      echo OK
    fi

    ;;

  restart)
    DROPWIZARD_SH=$0
    if [ ! -f $DROPWIZARD_SH ]; then
      if [ ! -f $DROPWIZARD_HOME/bin/dropwizard.sh ]; then
        echo "$DROPWIZARD_HOME/bin/dropwizard.sh does not exist."
        exit 1
      fi
      DROPWIZARD_SH=$DROPWIZARD_HOME/bin/dropwizard.sh
    fi

    "$DROPWIZARD_SH" stop "$@"
    "$DROPWIZARD_SH" start "$@"

    ;;

  supervise)
    #
    # Under control of daemontools supervise monitor which
    # handles restarts and shutdowns via the svc program.
    #
    exec "${RUN_CMD[@]}"

    ;;

  run|demo)
    echo "Running Dropwizard: "

    if [ -f "$DROPWIZARD_PID" ]
    then
      if running "$DROPWIZARD_PID"
      then
        echo "Already Running!"
        exit 1
      else
        # dead pid file - remove
        rm -f "$DROPWIZARD_PID"
      fi
    fi

    echo exec "${RUN_CMD[@]}"
    exec "${RUN_CMD[@]}"

    ;;

  check|status)
    echo "Checking arguments to Dropwizard: "
    echo "DROPWIZARD_HOME     =  $DROPWIZARD_HOME"
    echo "DROPWIZARD_CONF     =  $DROPWIZARD_CONF"
    echo "DROPWIZARD_RUN      =  $DROPWIZARD_RUN"
    echo "DROPWIZARD_PID      =  $DROPWIZARD_PID"
    echo "DROPWIZARD_PORT     =  $DROPWIZARD_PORT"
    echo "DROPWIZARD_LOGS     =  $DROPWIZARD_LOGS"
    echo "START_INI      =  $START_INI"
    echo "CONFIGS        =  ${CONFIGS[*]}"
    echo "JAVA_OPTIONS   =  ${JAVA_OPTIONS[*]}"
    echo "JAVA           =  $JAVA"
    echo "CLASSPATH      =  $CLASSPATH"
    echo "RUN_CMD        =  ${RUN_CMD[*]}"
    echo
    
    if [ -f "$DROPWIZARD_PID" ]
    then
      echo "Dropwizard running pid=$(< "$DROPWIZARD_PID")"
      exit 0
    fi
    exit 1

    ;;

  *)
    usage

    ;;
esac

exit 0
