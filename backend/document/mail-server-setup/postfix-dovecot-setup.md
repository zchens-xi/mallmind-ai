# Postfix + Dovecot 邮件服务器搭建指南

## 简介
Postfix (SMTP) + Dovecot (IMAP/POP3) 是Linux系统上最经典的邮件服务器组合。

## 优势
- 极其稳定可靠
- 性能优异
- 安全性高
- 社区支持完善
- 配置灵活

## 系统要求
- Ubuntu 20.04+ 或 CentOS 8+
- 至少1GB RAM
- 10GB+ 存储空间

## 安装步骤 (Ubuntu)

### 1. 更新系统并安装必要软件
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install postfix dovecot-core dovecot-imapd dovecot-lmtpd dovecot-mysql mysql-server
```

### 2. 配置MySQL数据库
```bash
sudo mysql_secure_installation

# 创建邮件数据库
sudo mysql -u root -p
```

```sql
CREATE DATABASE mailserver;
CREATE USER 'mailuser'@'localhost' IDENTIFIED BY 'secure_password';
GRANT SELECT ON mailserver.* TO 'mailuser'@'localhost';
FLUSH PRIVILEGES;

USE mailserver;

-- 域名表
CREATE TABLE virtual_domains (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  PRIMARY KEY (id)
);

-- 用户表
CREATE TABLE virtual_users (
  id INT NOT NULL AUTO_INCREMENT,
  domain_id INT NOT NULL,
  password VARCHAR(106) NOT NULL,
  email VARCHAR(120) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY email (email),
  FOREIGN KEY (domain_id) REFERENCES virtual_domains(id) ON DELETE CASCADE
);

-- 别名表
CREATE TABLE virtual_aliases (
  id INT NOT NULL AUTO_INCREMENT,
  domain_id INT NOT NULL,
  source VARCHAR(100) NOT NULL,
  destination VARCHAR(100) NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (domain_id) REFERENCES virtual_domains(id) ON DELETE CASCADE
);

-- 插入示例数据
INSERT INTO virtual_domains (name) VALUES ('yourdomain.com');
INSERT INTO virtual_users (domain_id, password, email) VALUES 
(1, ENCRYPT('password', CONCAT('$6$', SUBSTRING(SHA(RAND()), -16))), 'admin@yourdomain.com');
```

### 3. 配置Postfix
编辑 `/etc/postfix/main.cf`:
```bash
# 基本配置
myhostname = mail.yourdomain.com
myorigin = /etc/mailname
mydestination = localhost
relayhost = 
mynetworks = 127.0.0.0/8 [::ffff:127.0.0.0]/104 [::1]/128
mailbox_size_limit = 0
recipient_delimiter = +
inet_interfaces = all
inet_protocols = all

# 虚拟域配置
virtual_transport = lmtp:unix:private/dovecot-lmtp
virtual_mailbox_domains = mysql:/etc/postfix/mysql-virtual-mailbox-domains.cf
virtual_mailbox_maps = mysql:/etc/postfix/mysql-virtual-mailbox-maps.cf
virtual_alias_maps = mysql:/etc/postfix/mysql-virtual-alias-maps.cf

# SMTP认证
smtpd_sasl_type = dovecot
smtpd_sasl_path = private/auth
smtpd_sasl_auth_enable = yes
smtpd_sasl_security_options = noanonymous
smtpd_sasl_local_domain = $myhostname
smtpd_recipient_restrictions = permit_sasl_authenticated,permit_mynetworks,reject_unauth_destination

# TLS配置
smtpd_tls_security_level = may
smtpd_tls_auth_only = yes
smtpd_tls_cert_file = /etc/ssl/certs/mailcert.pem
smtpd_tls_key_file = /etc/ssl/private/mail.key
smtpd_tls_loglevel = 1
smtpd_tls_received_header = yes
smtpd_tls_session_cache_timeout = 3600s
```

创建MySQL查询配置文件：
```bash
# /etc/postfix/mysql-virtual-mailbox-domains.cf
user = mailuser
password = secure_password
hosts = localhost
dbname = mailserver
query = SELECT 1 FROM virtual_domains WHERE name='%s'

# /etc/postfix/mysql-virtual-mailbox-maps.cf
user = mailuser
password = secure_password
hosts = localhost
dbname = mailserver
query = SELECT 1 FROM virtual_users WHERE email='%s'

# /etc/postfix/mysql-virtual-alias-maps.cf
user = mailuser
password = secure_password
hosts = localhost
dbname = mailserver
query = SELECT destination FROM virtual_aliases WHERE source='%s'
```

### 4. 配置Dovecot
编辑主要配置文件：

`/etc/dovecot/dovecot.conf`:
```bash
protocols = imap lmtp
```

`/etc/dovecot/conf.d/10-mail.conf`:
```bash
mail_location = maildir:/var/mail/vhosts/%d/%n
mail_privileged_group = mail
```

`/etc/dovecot/conf.d/10-auth.conf`:
```bash
disable_plaintext_auth = yes
auth_mechanisms = plain login
!include auth-sql.conf.ext
```

`/etc/dovecot/conf.d/auth-sql.conf.ext`:
```bash
passdb {
  driver = sql
  args = /etc/dovecot/dovecot-sql.conf.ext
}

userdb {
  driver = static
  args = uid=mail gid=mail home=/var/mail/vhosts/%d/%n
}
```

`/etc/dovecot/dovecot-sql.conf.ext`:
```bash
driver = mysql
connect = host=localhost dbname=mailserver user=mailuser password=secure_password
default_pass_scheme = SHA512-CRYPT
password_query = SELECT email as user, password FROM virtual_users WHERE email='%u';
```

### 5. 创建邮件目录
```bash
sudo mkdir -p /var/mail/vhosts/yourdomain.com
sudo groupadd -g 5000 vmail
sudo useradd -g vmail -u 5000 vmail -d /var/mail
sudo chown -R vmail:vmail /var/mail
```

### 6. 生成SSL证书
使用Let's Encrypt:
```bash
sudo apt install certbot
sudo certbot certonly --standalone -d mail.yourdomain.com
sudo ln -s /etc/letsencrypt/live/mail.yourdomain.com/fullchain.pem /etc/ssl/certs/mailcert.pem
sudo ln -s /etc/letsencrypt/live/mail.yourdomain.com/privkey.pem /etc/ssl/private/mail.key
```

### 7. 启动服务
```bash
sudo systemctl restart postfix
sudo systemctl restart dovecot
sudo systemctl enable postfix
sudo systemctl enable dovecot
```

## Spring Boot 配置
```yaml
spring:
  mail:
    host: mail.yourdomain.com
    port: 587
    username: admin@yourdomain.com
    password: password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```
