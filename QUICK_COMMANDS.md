# VendorPlus Deployment - Quick Commands

# ====================
# 1. BUILD APPLICATION
# ====================
cd C:\Users\LENOVO\Desktop\vendorplus
ant dist

# This creates: dist\vendorplus.war


# ====================
# 2. EXPORT DATABASE
# ====================

# Option A: Use existing vendorplus.sql
# File: vp\vendorplus.sql (already exists)

# Option B: Export fresh from current database
& "C:\wamp64\bin\mariadb\mariadb10.3.14\bin\mysqldump.exe" -u root vendorplus > vendorplus_fresh.sql


# ====================
# 3. TRANSFER TO SERVER
# ====================

# After you create Linode server, replace YOUR_IP with actual IP:

# Transfer WAR file
scp dist\vendorplus.war vendorplus@YOUR_IP:/home/vendorplus/

# Transfer database
scp vp\vendorplus.sql vendorplus@YOUR_IP:/home/vendorplus/


# ====================
# 4. CONNECT TO SERVER
# ====================
ssh vendorplus@YOUR_IP


# ====================
# 5. ON SERVER - Deploy
# ====================

# Copy WAR to Tomcat
sudo cp /home/vendorplus/vendorplus.war /opt/tomcat/webapps/
sudo chown tomcat:tomcat /opt/tomcat/webapps/vendorplus.war

# Import database (after creating database and user)
mysql -u vendorplus -p vendorplus < /home/vendorplus/vendorplus.sql

# Update config
sudo nano /opt/tomcat/webapps/vendorplus/WEB-INF/classes/configurations/ConfigFile.properties

# Restart Tomcat
sudo systemctl restart tomcat

# Check logs
sudo tail -f /opt/tomcat/logs/catalina.out


# ====================
# 6. TEST APPLICATION
# ====================
# Open browser: http://YOUR_IP:8080/vendorplus


# ====================
# USEFUL COMMANDS
# ====================

# Check Tomcat status
sudo systemctl status tomcat

# Restart Tomcat
sudo systemctl restart tomcat

# View logs (live)
sudo tail -f /opt/tomcat/logs/catalina.out

# Check open ports
sudo netstat -tulpn | grep LISTEN

# Check disk space
df -h

# Check memory usage
free -m

# Backup database
mysqldump -u vendorplus -p vendorplus > backup_$(date +%Y%m%d).sql
