# Xpose Device

แอปเครื่องเสมือน (app clone) บน Android ที่เลือกได้ว่าแต่ละแอปจะ "เห็น" เครื่องเราเป็นรุ่นอะไร ใช้ปลดล็อกตัวเลือกกราฟิกในเกมที่เปิดให้เฉพาะรุ่นเครื่องที่อยู่ในรายชื่อ

พัฒนาต่อจาก [ZiTanIOI/BlackBox](https://github.com/ZiTanIOI/BlackBox) (fork ของ FBlackBox/BlackBox ที่รองรับ Android 5–16) ใต้ไลเซนส์ Apache 2.0

## ดาวน์โหลด

ทุกครั้งที่ push โค้ด GitHub Actions จะ build APK และลงไว้ที่หน้า **Releases** ให้อัตโนมัติ

1. เปิดหน้า [Releases ล่าสุด](../../releases/latest)
2. กดดาวน์โหลด `Xpose-Device-arm64.apk`
3. เปิดไฟล์เพื่อติดตั้ง (ถ้าเครื่องถาม ให้อนุญาตติดตั้งแอปจากแหล่งที่ไม่รู้จัก)

ทุก build ใช้กุญแจเซ็นแอปชุดเดียวกัน (`debug.keystore`) จึงติดตั้งทับเวอร์ชันเดิมได้เลย

## วิธีใช้

1. เปิดแอป กดปุ่ม + แล้วเลือกเกมที่จะ clone เข้ามา
2. **กดค้าง** ที่ไอคอนเกม แล้วเลือก **ปลอมรุ่นเครื่อง**
3. เลือกรุ่นที่ต้องการ แอปจะหยุดเกมให้อัตโนมัติ
4. เปิดเกมจากในแอป Xpose Device แล้วเข้าไปดูการตั้งค่ากราฟิก

รุ่นที่มีให้เลือก: Galaxy S25 Ultra, Galaxy S24 Ultra, ROG Phone 8 Pro, RedMagic 9 Pro, Xiaomi 14, OnePlus 12

## สิ่งที่ถูกเปลี่ยน

- ค่าใน `android.os.Build`: `BRAND`, `MANUFACTURER`, `MODEL`, `DEVICE`, `PRODUCT`
- ค่าที่อ่านผ่าน `SystemProperties.get` สำหรับคีย์ `ro.product.*` และชื่อทางการตลาด

ส่วนที่ **ไม่** เปลี่ยน: ชิป, GPU, เวอร์ชัน Android และค่าที่เกมอ่านผ่านโค้ด native (`__system_property_get`) ถ้าเกมเช็กชื่อ GPU หรืออ่านค่าจาก native โดยตรง วิธีนี้จะไม่ได้ผล

## ข้อควรรู้

- เกมออนไลน์ที่มีระบบกันโกงอาจตรวจพบว่ารันในเครื่องเสมือน และอาจเปิดไม่ได้หรือแบนบัญชี ควรทดลองกับบัญชีรองก่อน
- ถ้าเกมเด้งออกตอนเปิด ให้ลองตั้งกลับเป็น "เครื่องจริง (ปิด)" เพื่อดูว่าปัญหามาจากตัว clone หรือจากการปลอมรุ่น

## โค้ดที่เพิ่มเข้ามา

| ไฟล์ | หน้าที่ |
| --- | --- |
| `Bcore/.../core/device/DeviceProfile.java` | รายการรุ่นเครื่องและค่าของแต่ละรุ่น |
| `Bcore/.../core/device/DeviceProfileConfig.java` | บันทึกว่าแต่ละแอปเลือกรุ่นไหน |
| `Bcore/.../core/device/DeviceSpoofer.java` | เปลี่ยนค่าใน process ของแอปก่อนที่แอปจะเริ่มทำงาน |
| `app/.../view/apps/AppsFragment.kt` | เมนูเลือกรุ่นเครื่อง |

อยากเพิ่มรุ่นอื่นให้แก้ที่ `DeviceProfile.java` ในบล็อก `static { ... }`

## License

Apache License 2.0 ดูไฟล์ [LICENSE](LICENSE) ส่วน README เดิมของโปรเจกต์ต้นฉบับอยู่ที่ [README_UPSTREAM.md](README_UPSTREAM.md)
