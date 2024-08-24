# Hệ Thống Giám Sát Chất Lượng Không Khí 

## Giới Thiệu

Dự án này xây dựng một hệ thống giám sát chất lượng không khí sử dụng công nghệ IoT và các cảm biến môi trường. Hệ thống có khả năng thu thập dữ liệu từ các cảm biến chất lượng không khí và truyền tải thông tin đến một máy chủ trung tâm. Người dùng có thể theo dõi dữ liệu theo thời gian thực thông qua một ứng dụng web.

## Tính Năng

- **Giám sát chất lượng không khí theo thời gian thực**: Hệ thống sử dụng các cảm biến để đo các chỉ số như CO, Gas, nhiệt độ, độ ẩm.
- **Lưu trữ dữ liệu**: Dữ liệu từ các cảm biến được lưu trữ trong cơ sở dữ liệu để phân tích và theo dõi.
- **Giao diện web**: Người dùng có thể xem dữ liệu chất lượng không khí trên một giao diện web, với biểu đồ và báo cáo chi tiết.
- **Cảnh báo**: Hệ thống có thể gửi cảnh báo qua email và ngay tại trên thiết bị khi các chỉ số vượt quá ngưỡng an toàn được thiết lập.
- **Cập nhật phầm mềm thiết bị**: Chức năng giúp cho quản trị viên gửi các phiên bản firmware lên hệ thống cũng như người dùng có thể cập nhật firmware xuống thiết bị

## Cấu Trúc Dự Án

- **Firmware**: Mã nguồn cho các thiết bị IoT và cảm biến, bao gồm kết nối Wi-Fi, thu thập dữ liệu, và gửi dữ liệu về máy chủ.
- **Backend**: Hệ thống máy chủ được xây dựng bằng Spring Boot để xử lý và lưu trữ dữ liệu từ các thiết bị.
- **Frontend**: Ứng dụng web cho người dùng theo dõi dữ liệu chất lượng không khí.
- **Database**: Cơ sở dữ liệu để lưu trữ các dữ liệu cảm biến và người dùng.

## Công Nghệ Sử Dụng

- **Hardware**: ESP8266/ESP32, cảm biến chất lượng không khí (PM2.5, PM10, CO2, nhiệt độ, độ ẩm).
- **Backend**: Java, Spring Boot, REST API.
- **Frontend**: HTML, CSS, JavaScript, Bootstrap.
- **Database**: MySQL.
- **IoT Platform**: MQTT, HTTP.

## Cài Đặt

### Yêu Cầu

- **Phần cứng**: Các cảm biến, ESP8266, và các module cần thiết.
- **Phần mềm**: Java 8+, Maven, MySQL.

### Hướng Dẫn Cài Đặt

1. **Clone dự án về máy:**
   ```bash
   git clone https://github.com/Khanhagtm/Air-Quality-Management.git
   
