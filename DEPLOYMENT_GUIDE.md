# VendorPlus Deployment Guide - Linode

## Part 1: Create Linode Server

### Step 1: Sign Up & Create Server
1. Go to https://linode.com and create an account
2. Click **"Create Linode"**
3. Choose:
   - **Distribution**: Ubuntu 22.04 LTS
   - **Region**: Choose closest to Uganda (e.g., Frankfurt, Germany or Mumbai, India)
   - **Plan**: Shared CPU - **Linode 4GB** ($24/mo recommended for production)
   - **Label**: vendorplus-prod
   - **Root Password**: Create a strong password (SAVE THIS!)
4. Click **"Create Linode"**
5. Wait 2-3 minutes for server to boot

### Step 2: Get Your Server IP
- Once running, note your **IP Address** (e.g., 123.45.67.89)

---

## Part 2: Initial Server Setup

### Step 1: Connect via SSH
On your Windows PC, open PowerShell and connect:

```powershell
ssh root@YOUR_IP_ADDRESS
```

Enter the root password when prompted.

### Step 2: Update System
```bash
apt update && apt upgrade -y
```

### Step 3: Create Non-Root User
```bash
# Create user
adduser vendorplus
# Enter password when prompted

# Add to sudo group
usermod -aG sudo vendorplus

# Switch to new user
su - vendorplus
```

### Step 4: Setup Firewall
```bash
sudo ufw allow OpenSSH
sudo ufw allow 8080/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
# Type 'y' to confirm
```

---

## Part 3: Install Required Software

### Step 1: Install Java 11
```bash
sudo apt install openjdk-11-jdk -y
java -version  # Verify installation
```

### Step 2: Install MariaDB
```bash
sudo apt install mariadb-server -y
sudo mysql_secure_installation
```

**Configuration prompts:**
- Switch to unix_socket authentication? **N**
- Change root password? **Y** (set a strong password)
- Remove anonymous users? **Y**
- Disallow root login remotely? **Y**
- Remove test database? **Y**
- Reload privilege tables? **Y**

### Step 3: Install Tomcat 9
```bash
# Create tomcat user
sudo useradd -r -m -U -d /opt/tomcat -s /bin/false tomcat

# Download Tomcat
cd /tmp
wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.80/bin/apache-tomcat-9.0.80.tar.gz

# Extract to /opt/tomcat
sudo mkdir -p /opt/tomcat
sudo tar xzvf apache-tomcat-9.0.80.tar.gz -C /opt/tomcat --strip-components=1

# Set permissions
sudo chown -R tomcat:tomcat /opt/tomcat
sudo chmod -R u+x /opt/tomcat/bin
```

### Step 4: Configure Tomcat as Service
```bash
sudo nano /etc/systemd/system/tomcat.service
```

**Paste this content:**
```ini
[Unit]
Description=Apache Tomcat Web Application Container
After=network.target

[Service]
Type=forking

User=tomcat
Group=tomcat

Environment="JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64"
Environment="CATALINA_PID=/opt/tomcat/temp/tomcat.pid"
Environment="CATALINA_HOME=/opt/tomcat"
Environment="CATALINA_BASE=/opt/tomcat"
Environment="CATALINA_OPTS=-Xms512M -Xmx2048M -server -XX:+UseParallelGC"

ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh

RestartSec=10
Restart=always

[Install]
WantedBy=multi-user.target
```

**Save with:** Ctrl+O, Enter, Ctrl+X

```bash
# Reload systemd and start Tomcat
sudo systemctl daemon-reload
sudo systemctl start tomcat
sudo systemctl enable tomcat
sudo systemctl status tomcat  # Check it's running
```

---

## Part 4: Deploy VendorPlus Application

### Step 1: Build Your Application (On Your PC)
On your Windows PC, open PowerShell in your project folder:

```powershell
cd C:\Users\LENOVO\Desktop\vendorplus
ant dist
```

This creates `dist\vendorplus.war`

### Step 2: Transfer WAR File to Server
```powershell
# Replace YOUR_IP with your Linode IP
scp dist\vendorplus.war vendorplus@YOUR_IP:/home/vendorplus/
```

### Step 3: Deploy to Tomcat (On Server)
Back in your SSH session:

```bash
# Copy WAR to Tomcat webapps
sudo cp /home/vendorplus/vendorplus.war /opt/tomcat/webapps/

# Set ownership
sudo chown tomcat:tomcat /opt/tomcat/webapps/vendorplus.war

# Tomcat will auto-deploy in ~30 seconds
```

---

## Part 5: Setup Database

### Step 1: Create Database
```bash
sudo mysql -u root -p
```

**In MySQL prompt:**
```sql
-- Create database
CREATE DATABASE vendorplus CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'vendorplus'@'localhost' IDENTIFIED BY 'YourStrongPassword123!';

-- Grant permissions
GRANT ALL PRIVILEGES ON vendorplus.* TO 'vendorplus'@'localhost';
FLUSH PRIVILEGES;

-- Exit
EXIT;
```

### Step 2: Import Database (On Your PC)
```powershell
# Transfer SQL file to server
scp vp\vendorplus.sql vendorplus@YOUR_IP:/home/vendorplus/
```

### Step 3: Import SQL (On Server)
```bash
mysql -u vendorplus -p vendorplus < /home/vendorplus/vendorplus.sql
# Enter the vendorplus database password when prompted
```

### Step 4: Update Database Credentials
```bash
# Edit config file
sudo nano /opt/tomcat/webapps/vendorplus/WEB-INF/classes/configurations/ConfigFile.properties
```

**Update these lines:**
```properties
branch_host=localhost
branch_database=vendorplus
branch_user=vendorplus
branch_password=YourStrongPassword123!
```

**Save:** Ctrl+O, Enter, Ctrl+X

### Step 5: Restart Tomcat
```bash
sudo systemctl restart tomcat
```

---

## Part 6: Access Your Application

### Option A: Access via IP (Temporary)
Open browser: `http://YOUR_IP:8080/vendorplus`

### Option B: Setup Domain Name (Recommended)

#### Step 1: Point Domain to Server
In your domain registrar (Namecheap, GoDaddy, etc.):
- Create **A Record**: `@` → `YOUR_IP`
- Create **A Record**: `www` → `YOUR_IP`

#### Step 2: Install Nginx (Reverse Proxy)
```bash
sudo apt install nginx -y
sudo nano /etc/nginx/sites-available/vendorplus
```

**Paste:**
```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    location / {
        proxy_pass http://localhost:8080/vendorplus/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**Enable site:**
```bash
sudo ln -s /etc/nginx/sites-available/vendorplus /etc/nginx/sites-enabled/
sudo nginx -t  # Test configuration
sudo systemctl restart nginx
```

#### Step 3: Setup SSL (Free HTTPS)
```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

Follow prompts. Your site will now be accessible at: `https://yourdomain.com`

---

## Part 7: Enable Automatic Backups

### Database Backup Script
```bash
sudo nano /home/vendorplus/backup_db.sh
```

**Paste:**
```bash
#!/bin/bash
BACKUP_DIR="/home/vendorplus/backups"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

mysqldump -u vendorplus -pYourStrongPassword123! vendorplus > $BACKUP_DIR/vendorplus_$DATE.sql

# Keep only last 7 days
find $BACKUP_DIR -name "vendorplus_*.sql" -mtime +7 -delete
```

**Make executable:**
```bash
chmod +x /home/vendorplus/backup_db.sh
```

**Schedule daily backups:**
```bash
crontab -e
# Choose nano (option 1)
```

**Add this line (runs daily at 2 AM):**
```
0 2 * * * /home/vendorplus/backup_db.sh
```

---

## Part 8: Monitoring & Maintenance

### Check Application Logs
```bash
# Tomcat logs
sudo tail -f /opt/tomcat/logs/catalina.out

# Application logs
sudo tail -f /opt/tomcat/webapps/vendorplus/WEB-INF/classes/logs/*.log
```

### Check Tomcat Status
```bash
sudo systemctl status tomcat
```

### Restart Services
```bash
# Restart Tomcat
sudo systemctl restart tomcat

# Restart Nginx
sudo systemctl restart nginx

# Restart Database
sudo systemctl restart mariadb
```

---

## Quick Reference

### Server Details
- **IP**: YOUR_IP_ADDRESS
- **SSH User**: vendorplus
- **App URL**: http://YOUR_IP:8080/vendorplus
- **Tomcat**: /opt/tomcat
- **Logs**: /opt/tomcat/logs/

### Default Credentials
- **Database User**: vendorplus
- **Database**: vendorplus

---

## Troubleshooting

### Application Won't Start
```bash
# Check Tomcat logs
sudo tail -100 /opt/tomcat/logs/catalina.out

# Check if Tomcat is running
sudo systemctl status tomcat

# Restart Tomcat
sudo systemctl restart tomcat
```

### Database Connection Errors
```bash
# Test database connection
mysql -u vendorplus -p vendorplus

# Check database is running
sudo systemctl status mariadb
```

### Can't Access from Browser
```bash
# Check firewall
sudo ufw status

# Check Tomcat is listening
sudo netstat -tulpn | grep 8080

# Check Nginx (if using domain)
sudo nginx -t
sudo systemctl status nginx
```

---

## Security Best Practices

1. **Change default passwords** for database and admin users
2. **Enable firewall** (already done in Step 2.4)
3. **Use SSL/HTTPS** for production (Part 6, Step 3)
4. **Regular backups** (Part 7)
5. **Keep system updated**:
   ```bash
   sudo apt update && sudo apt upgrade -y
   ```
6. **Monitor logs regularly**
7. **Disable root SSH** (optional, but secure):
   ```bash
   sudo nano /etc/ssh/sshd_config
   # Set: PermitRootLogin no
   sudo systemctl restart sshd
   ```

---

## Next Steps After Deployment

1. Test all application features
2. Import real data if different from vendorplus.sql
3. Configure email settings (if application sends emails)
4. Setup monitoring (Linode has built-in monitoring)
5. Create application admin users
6. Train users on new URL/domain

---

**Need Help?** Check Tomcat logs first, they usually show the exact error.
