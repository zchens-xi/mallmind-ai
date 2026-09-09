# Postal 邮件服务器搭建指南

## 简介
Postal 是一个完整的开源邮件发送平台，专为现代应用程序设计。

## 优势
- 现代化Web界面
- 强大的API支持
- 详细的邮件统计和监控
- 支持DKIM、SPF、DMARC
- 内置反垃圾邮件机制
- Docker部署简单

## 系统要求
- Ubuntu 18.04+ 或 CentOS 7+
- 至少2GB RAM
- 20GB+ 存储空间
- Docker 和 Docker Compose

## 快速部署 (Docker)

### 1. 创建部署目录
```bash
mkdir /opt/postal
cd /opt/postal
```

### 2. 创建 docker-compose.yml
```yaml
version: '3.8'
services:
  postal:
    image: postal/postal:2.1.2
    container_name: postal
    hostname: postal.yourdomain.com
    restart: unless-stopped
    ports:
      - "25:25"     # SMTP
      - "587:587"   # SMTP Submission
      - "5000:5000" # Web Interface
    volumes:
      - postal-data:/opt/postal
      - postal-config:/config
    environment:
      - POSTAL_MYSQL_HOST=mysql
      - POSTAL_MYSQL_DATABASE=postal
      - POSTAL_MYSQL_USERNAME=postal
      - POSTAL_MYSQL_PASSWORD=your_secure_password
      - POSTAL_RABBITMQ_HOST=rabbitmq
      - POSTAL_WEB_HOSTNAME=postal.yourdomain.com
    depends_on:
      - mysql
      - rabbitmq

  mysql:
    image: mysql:8.0
    container_name: postal-mysql
    restart: unless-stopped
    environment:
      - MYSQL_ROOT_PASSWORD=root_password
      - MYSQL_DATABASE=postal
      - MYSQL_USER=postal
      - MYSQL_PASSWORD=your_secure_password
    volumes:
      - mysql-data:/var/lib/mysql

  rabbitmq:
    image: rabbitmq:3-management
    container_name: postal-rabbitmq
    restart: unless-stopped
    environment:
      - RABBITMQ_DEFAULT_USER=postal
      - RABBITMQ_DEFAULT_PASS=your_rabbitmq_password
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq

volumes:
  postal-data:
  postal-config:
  mysql-data:
  rabbitmq-data:
```

### 3. 启动服务
```bash
docker-compose up -d
```

### 4. 初始化配置
```bash
# 进入容器
docker exec -it postal bash

# 初始化数据库
postal initialize

# 创建管理员用户
postal make-user
```

## DNS配置
在您的域名DNS中添加以下记录：

### MX记录
```
@ MX 10 mail.yourdomain.com
```

### A记录
```
mail.yourdomain.com A your_server_ip
postal.yourdomain.com A your_server_ip
```

### SPF记录
```
@ TXT "v=spf1 mx ip4:your_server_ip ~all"
```

### DKIM记录 (在Postal界面生成后添加)
```
postal._domainkey TXT "your_dkim_key"
```

### DMARC记录
```
_dmarc TXT "v=DMARC1; p=none; rua=mailto:dmarc@yourdomain.com"
```

## 访问和配置
1. 访问 http://postal.yourdomain.com:5000
2. 使用创建的管理员账号登录
3. 创建组织和邮件服务器
4. 获取API密钥用于应用程序集成

## Spring Boot 集成配置
```yaml
spring:
  mail:
    host: mail.yourdomain.com
    port: 587
    username: your_smtp_username
    password: your_smtp_password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```
