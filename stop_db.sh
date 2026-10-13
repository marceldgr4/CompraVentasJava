#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "Deteniendo PostgreSQL local en puerto 5433..."
/Library/PostgreSQL/18/bin/pg_ctl -D "$DIR/data_local" stop
