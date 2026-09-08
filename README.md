# Sổ Nợ

Ứng dụng Android nhỏ gọn, nhanh và đẹp để theo dõi nợ cá nhân — trả lời hai câu hỏi: **"Ai nợ tôi?"** và **"Tôi nợ ai?"**.

Hoạt động hoàn toàn offline, dữ liệu lưu cục bộ. Không tài khoản, không backend, không đồng bộ.

## Tính năng

- Thêm / sửa / xóa khoản nợ theo từng người
- Hai hướng nợ: **Họ nợ tôi** / **Tôi nợ họ** (phân biệt bằng màu + ký hiệu + nhãn)
- Tổng hợp nhanh: tổng "Họ nợ bạn", tổng "Bạn nợ", và "Chênh lệch"
- Định dạng tiền VND (`1.500.000 ₫`), không dùng số thập phân, lưu bằng `Long`
- Avatar từ Android Photo Picker (không cần quyền lưu trữ) hoặc placeholder chữ cái đầu theo tên
- Tìm kiếm theo tên, sắp xếp (Gần đây / Số tiền lớn nhất / Tên A–Z)
- Chế độ giao diện: Theo hệ thống / Sáng / Tối (lưu lựa chọn)
- Giao diện tiếng Việt, hỗ trợ dark mode và accessibility

## Công nghệ

- Kotlin + Jetpack Compose (Material 3)
- Room (lưu trữ cục bộ)
- DataStore Preferences (lưu chủ đề)
- Android Photo Picker (`PickVisualMedia`)
- Gradle Kotlin DSL + version catalog

## Cấu trúc dự án

```
app/src/main/java/com/nomono/sono/
  MainActivity.kt          # entry point
  SonoApp.kt               # Application, khởi tạo DB/repository/preferences
  data/                    # Debt, DebtDao, AppDatabase, DebtRepository, ThemePreferences
  ui/
    theme/                 # màu sắc, typography, shape, SonoTheme
    home/                  # HomeScreen + HomeViewModel (danh sách, tổng, tìm kiếm, sắp xếp)
    edit/                  # DebtEditorSheet (thêm/sửa/xóa, chọn avatar)
    components/            # Avatar (placeholder + ảnh)
  util/                    # VndFormat, DebtTotals, Validation, VndGroupingTransformation
app/src/test/...           # unit tests cho định dạng VND, tổng nợ, validation
```

## Chạy & build

Cần JDK 17, Android SDK (platform 35). Cấu hình đường dẫn SDK trong `local.properties`.

```powershell
$env:JAVA_HOME="D:\android-tools\jdk"; $env:ANDROID_HOME="D:\android-tools\sdk"
cd D:\Nomono
.\gradlew.bat assembleDebug
```

Build APK debug:

```powershell
.\gradlew.bat assembleDebug
```

APK nằm tại: `app/build/outputs/apk/debug/app-debug.apk`

Chạy test:

```powershell
.\gradlew.bat test
```
