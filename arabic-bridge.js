// سكريبت التعريب التلقائي وتحويل الحفظ إلى ذاكرة الهاتف الداخلية
window.addEventListener('DOMContentLoaded', () => {
    console.log("FUTO Theme Editor - Arabic & Android Bridge Loaded");

    // اعتراض وظائف التنزيل لكي يتم الحفظ مباشرة عبر الـ AndroidInterface
    const originalCreateElement = document.createElement;
    document.createElement = function(tagName) {
        const element = originalCreateElement.call(document, tagName);
        if (tagName.toLowerCase() === 'a') {
            const originalSetAttribute = element.setAttribute;
            element.setAttribute = function(name, value) {
                if (name === 'href' && value.startsWith('blob:')) {
                    // اعتراض رابط الـ Blob وتحويله إلى Base64 للإرسال لتطبيق الأندرويد
                    setTimeout(async () => {
                        try {
                            const response = await fetch(value);
                            const blob = await response.blob();
                            const reader = new FileReader();
                            reader.onloadend = function() {
                                const base64data = reader.result;
                                const filename = element.download || "futo_theme.zip";
                                if (window.AndroidInterface) {
                                    window.AndroidInterface.saveThemeZip(base64data, filename);
                                }
                            };
                            reader.readAsDataURL(blob);
                        } catch(err) {
                            console.error("Download intercept error:", err);
                        }
                    }, 100);
                }
                return originalSetAttribute.apply(this, arguments);
            };
        }
        return element;
    };

    // قاموس التعريب الواسع لعناصر القوائم والأزرار في المشروع الأصلي
    setTimeout(() => {
        const translations = {
            "File": "ملف",
            "Edit": "تحرير",
            "View": "عرض",
            "Insert": "إدراج",
            "Help": "مساعدة",
            "New project": "مشروع جديد",
            "Open project": "فتح مشروع",
            "Save": "حفظ",
            "Export": "تصدير",
            "Theme Editor": "محرر الثيمات",
            "Colors": "الألوان",
            "Assets": "الأصول",
            "Settings": "الإعدادات",
            "Background": "الخلفية",
            "Keyboard Surface": "خلفية اللوحة",
            "Keyboard Container": "حاوية المفاتيح",
            "Text Color": "لون النصوص",
            "Primary": "الرئيسي",
            "Author": "المصمم",
            "Version": "الإصدار",
            "Name": "الاسم"
        };

        // دالة استبدال النصوص البسيطة في الصفحة
        function walkTextNodes(node) {
            if (node.nodeType === Node.TEXT_NODE) {
                let text = node.nodeValue.trim();
                if (translations[text]) {
                    node.nodeValue = node.nodeValue.replace(text, translations[text]);
                }
            } else {
                for (let child of node.childNodes) {
                    walkTextNodes(child);
                }
            }
        }
        walkTextNodes(document.body);
    }, 1500);
});
