### PostgreSQL

sudo service postgresql status
sudo service postgresql start
sudo service postgresql stop
sudo -u postgres psql
CREATE USER task_manager_user WITH PASSWORD 'java-heslo';
CREATE DATABASE task_manager OWNER task_manager_user;
\q
