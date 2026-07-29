# BinaryEye Callback Server

## 项目简介

本项目用于接收 BinaryEye Android 应用通过蓝牙或网络发送的扫描数据。

**BinaryEye** 是一个开源的 Android 条码/二维码扫描应用，支持通过蓝牙将扫描结果发送到配对的设备。

## BinaryEye 数据传输机制

BinaryEye 通过以下方式发送扫描数据：

### 1. 蓝牙传输 (Bluetooth)
- **协议**: RFCOMM (SPP - Serial Port Profile)
- **UUID**: `8a8478c9-2ca8-404b-a0de-101f34ab71ae`
- **数据格式**: 纯文本，每条消息后跟换行符 `\n`

### 2. Deep Link (网页回调)
BinaryEye 支持通过 URL 参数将扫描结果回调到指定服务器：
```
http://your-server.com/scan?result={RESULT}&format={FORMAT}
```

## 功能特性

✅ **HTTP API 接口** - 接收 JSON 或文本格式的扫描数据  
✅ **历史记录管理** - 保存和查询扫描历史  
✅ **蓝牙服务支持** - 接收蓝牙传输的数据 (Windows/Linux)  
✅ **状态监控** - 提供健康检查和状态接口  
✅ **跨域支持** - 配置了 CORS 支持  

## 快速开始

### 1. 编译项目

```bash
cd binary_eys_callback
mvn clean package
```

### 2. 运行服务

```bash
java -jar target/binaryeye-callback-1.0.0.jar
```

服务将在 `http://localhost:8080` 启动。

### 3. 使用 API

#### 接收扫描数据 (JSON)
```bash
curl -X POST http://localhost:8080/api/scan \
  -H "Content-Type: application/json" \
  -d '{
    "content": "https://example.com",
    "format": "QR_CODE",
    "hexBytes": "68747470733a2f2f6578616d706c652e636f6d"
  }'
```

#### 接收扫描数据 (文本)
```bash
curl -X POST "http://localhost:8080/api/scan/text?content=https://example.com&format=QR_CODE"
```

#### 查询历史记录
```bash
# 获取所有历史
curl http://localhost:8080/api/scans

# 获取最近10条
curl "http://localhost:8080/api/scans?limit=10"
```

#### 健康检查
```bash
curl http://localhost:8080/api/health
```

#### 获取服务状态
```bash
curl http://localhost:8080/api/status
```

## API 文档

### 端点列表

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/scan` | 接收 JSON 格式的扫描数据 |
| POST | `/api/scan/text` | 接收文本格式的扫描数据 |
| GET | `/api/scans` | 获取扫描历史记录 |
| GET | `/api/scans/{index}` | 获取指定索引的扫描记录 |
| DELETE | `/api/scans` | 清空扫描历史 |
| GET | `/api/status` | 获取服务状态 |
| GET | `/api/health` | 健康检查 |

### 数据模型

```json
{
  "content": "扫描内容",
  "format": "条码格式 (如 QR_CODE, CODE_128)",
  "hexBytes": "原始字节的十六进制字符串",
  "timestamp": 1234567890123,
  "deviceName": "来源设备名称",
  "deviceAddress": "蓝牙地址"
}
```

## 配置说明

在 `application.properties` 中可以修改以下配置：

```properties
# 服务端口
server.port=8080

# 蓝牙服务开关
bluetooth.enabled=false

# 蓝牙服务名称
bluetooth.server-name=BinaryEye-Receiver
```

## BinaryEye 应用配置

BinaryEye 支持通过 Deep Link 将扫描结果回调到 HTTP 服务器。

### 使用 Deep Link 回调

在 BinaryEye 应用中配置 URL 回调：

1. 打开 BinaryEye 应用
2. 进入设置 (Settings)
3. 找到 Deep Link 相关设置（可能需要通过 Intent 方式调用）

#### URL 格式

使用以下 URL 模板调用 BinaryEye：

```
binaryeye://scan?ret=http%3A%2F%2FYOUR_SERVER%3APORT%2F%3Fcontent%3D{RESULT}%26format%3D{FORMAT}
```

示例（假设你的服务器地址是 192.168.1.100:8088）：

```
binaryeye://scan?ret=http%3A%2F%2F192.168.1.100%3A8088%2F%3Fcontent%3D{RESULT}%26format%3D{FORMAT}
```

**注意：** 此处使用根路径 `/?content=` 而非 `/api/scan?content=`，因为 BinaryEye Deep Link 的 `ret` 参数在某些情况下会添加逗号前缀，使用根路径可以自动过滤。

支持的参数：
- `{RESULT}` - 扫描内容
- `{FORMAT}` - 条码格式 (如 QR_CODE, CODE_128)
- `{RESULT_BYTES}` - 原始字节的十六进制字符串

### 通过第三方工具暴露本地服务

如果你的服务运行在本地网络，可以使用 ngrok 将本地端口暴露到公网：

1. 下载并安装 ngrok: https://ngrok.com/download
2. 注册账号并获取 authtoken
3. 配置 authtoken: `ngrok config add-authtoken YOUR_TOKEN`
4. 启动 ngrok 转发: `ngrok http 8088`
5. 使用生成的公网地址配置 BinaryEye

```bash
# 安装 ngrok (macOS)
brew install ngrok

# 配置 authtoken
ngrok config add-authtoken YOUR_TOKEN

# 启动转发
ngrok http 8088

# 会看到类似输出：
# Forwarding  https://abc123.ngrok.io -> http://localhost:8088
```

然后在 BinaryEye 中使用：
```
binaryeye://scan?ret=https%3A%2F%2Fabc123.ngrok.io%2Fapi%2Fscan%3Fcontent%3D{RESULT}%26format%3D{FORMAT}
```

### 替代方案：使用 Android Intent

如果你有开发能力，可以创建一个 Android 应用作为桥接，接收 BinaryEye 的 Intent 并转发到 HTTP API。

步骤：
1. 在 AndroidManifest.xml 中配置 Intent Filter
2. 处理 `SCAN` Intent
3. 将数据通过 HTTP POST 发送到你的服务器

## 技术栈

- **Java 11**
- **Spring Boot 2.7.14**
- **Maven**
- **Lombok**
- **BlueCove** (蓝牙支持)

## 项目结构

```
binary_eys_callback/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/binaryeyecallback/
│       │       ├── BinaryEyeCallbackApplication.java
│       │       ├── controller/
│       │       │   └── BinaryEyeController.java
│       │       ├── model/
│       │       │   └── ScanResult.java
│       │       └── service/
│       │           ├── ScanResultService.java
│       │           └── BluetoothService.java
│       └── resources/
│           └── application.properties
└── README.md
```

## 注意事项

⚠️ **macOS 蓝牙支持**: BlueCove 库在 macOS 上有兼容性问题。如果在 macOS 上使用，请将 `bluetooth.enabled` 设置为 `false`，仅使用 HTTP API。

✅ **Windows/Linux**: 完整支持蓝牙接收功能。

## License

MIT License
