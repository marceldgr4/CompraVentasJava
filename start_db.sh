#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
if /Library/PostgreSQL/18/bin/pg_isready -p 5433 >/dev/null 2>&1; then
    echo "Base de datos PostgreSQL local ya está corriendo en el puerto 5433."
else
    echo "Iniciando PostgreSQL local en puerto 5433..."
    /Library/PostgreSQL/18/bin/pg_ctl -D "$DIR/data_local" -l "$DIR/data_local/pg.log" -o "-p 5433" start
fi
