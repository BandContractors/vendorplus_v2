# VendorPlus Deployment Checklist

## Pre-Deployment
- [ ] Sign up for Linode account
- [ ] Add payment method
- [ ] Choose server location (Frankfurt/Mumbai recommended)
- [ ] Build WAR file: `ant dist`
- [ ] Backup current database
- [ ] Export database: Export from WAMP phpMyAdmin or use vendorplus.sql

## Server Setup
- [ ] Create Linode 4GB server (Ubuntu 22.04)
- [ ] Note server IP address: ___________________
- [ ] Connect via SSH
- [ ] Update system: `apt update && apt upgrade -y`
- [ ] Create vendorplus user
- [ ] Setup firewall (ufw)

## Software Installation
- [ ] Install Java 11
- [ ] Install MariaDB
- [ ] Secure MariaDB installation
- [ ] Install Tomcat 9
- [ ] Configure Tomcat service
- [ ] Start and enable Tomcat

## Application Deployment
- [ ] Transfer vendorplus.war to server
- [ ] Deploy WAR to Tomcat webapps
- [ ] Verify auto-deployment (check /opt/tomcat/webapps/)

## Database Setup
- [ ] Create vendorplus database
- [ ] Create vendorplus database user
- [ ] Transfer vendorplus.sql to server
- [ ] Import database
- [ ] Update ConfigFile.properties with database credentials
- [ ] Restart Tomcat

## Testing
- [ ] Access application: http://YOUR_IP:8080/vendorplus
- [ ] Test login
- [ ] Test creating transaction
- [ ] Test database connectivity
- [ ] Check application logs for errors

## Domain & SSL (Optional but Recommended)
- [ ] Point domain to server IP
- [ ] Install Nginx
- [ ] Configure reverse proxy
- [ ] Test domain access: http://yourdomain.com
- [ ] Install SSL certificate (certbot)
- [ ] Test HTTPS: https://yourdomain.com

## Backups & Monitoring
- [ ] Setup database backup script
- [ ] Schedule daily backups (cron)
- [ ] Test backup restoration
- [ ] Enable Linode backups (in Linode dashboard)

## Security Hardening
- [ ] Change default database passwords
- [ ] Disable root SSH login
- [ ] Configure fail2ban (optional)
- [ ] Setup monitoring alerts

## Post-Deployment
- [ ] Document server credentials (securely)
- [ ] Train users on new URL
- [ ] Monitor logs for first 24 hours
- [ ] Test all critical features
- [ ] Celebrate! 🎉

---

## Important Information to Save

**Server Details:**
- Linode IP: ___________________
- SSH User: vendorplus
- SSH Password: ___________________

**Database:**
- Database Name: vendorplus
- DB Username: vendorplus  
- DB Password: ___________________

**Application:**
- URL: http://___________________:8080/vendorplus
- Domain (if setup): https://___________________

**Backup Location:**
- Server: /home/vendorplus/backups/
- Linode Backups: Enabled in dashboard

---

## Emergency Contacts
- Linode Support: https://cloud.linode.com/support/tickets
- Server Restart: `sudo systemctl restart tomcat`
- Check Status: `sudo systemctl status tomcat`
