import 'package:flutter/material.dart';
import 'package:flutter_colorpicker/flutter_colorpicker.dart';
import '../models/theme_project.dart';
import '../widgets/keyboard_preview.dart';

class EditorScreen extends StatefulWidget {
  const EditorScreen({super.key, required this.project, required this.onChanged});
  final ThemeProject project;
  final ValueChanged<ThemeProject> onChanged;

  @override
  State<EditorScreen> createState() => _EditorScreenState();
}

class _EditorScreenState extends State<EditorScreen> {
  late ThemeProject p;
  String section = 'Colors';

  @override
  void initState() { super.initState(); p = widget.project; }

  void update(void Function(ThemeProject) change) {
    setState(() => change(p));
    widget.onChanged(p);
  }

  Future<void> chooseColor(String label, Color current, ValueChanged<Color> apply) async {
    Color chosen = current;
    await showDialog<void>(context: context, builder: (context) => AlertDialog(
      title: Text('اختيار اللون — $label'),
      content: SingleChildScrollView(child: ColorPicker(
        pickerColor: current, onColorChanged: (c) => chosen = c,
        enableAlpha: false, displayThumbColor: true,
      )),
      actions: [
        TextButton(onPressed: () => Navigator.pop(context), child: const Text('إلغاء')),
        FilledButton(onPressed: () { apply(chosen); Navigator.pop(context); }, child: const Text('تطبيق')),
      ],
    ));
  }

  @override
  Widget build(BuildContext context) {
    final sections = ['Colors','Gradients','Shapes','Backgrounds','Fonts','Assets','Matchrules','Keys','Effects','Animations','Sounds','Layout','Metadata'];
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Row(children: [
          Expanded(child: Text(p.name, style: Theme.of(context).textTheme.headlineSmall)),
          IconButton(tooltip: 'تراجع عن الإعدادات الأساسية', onPressed: () => update((p) {
            p.primary = const Color(0xFF16D9F2); p.secondary = const Color(0xFF8B5CF6);
          }), icon: const Icon(Icons.restart_alt)),
        ]),
        const SizedBox(height: 12),
        KeyboardPreview(project: p),
        const SizedBox(height: 16),
        Wrap(spacing: 8, runSpacing: 8, children: sections.map((s) => ChoiceChip(
          label: Text(s), selected: section == s, onSelected: (_) => setState(() => section = s),
        )).toList()),
        const SizedBox(height: 14),
        _panelForSection(),
      ],
    );
  }

  Widget _panelForSection() {
    switch (section) {
      case 'Colors':
        return Column(children: [
          _colorTile('لون أساسي', p.primary, (c) => update((p) => p.primary = c)),
          _colorTile('اللون الثانوي', p.secondary, (c) => update((p) => p.secondary = c)),
          _colorTile('خلفية لوحة المفاتيح', p.background, (c) => update((p) => p.background = c)),
          _colorTile('لون المفتاح', p.keyColor, (c) => update((p) => p.keyColor = c)),
          _colorTile('لون النص', p.keyText, (c) => update((p) => p.keyText = c)),
          _colorTile('الأزرار الوظيفية', p.functionKey, (c) => update((p) => p.functionKey = c)),
          _colorTile('لون الحدود', p.outline, (c) => update((p) => p.outline = c)),
        ]);
      case 'Gradients':
        return _card('التدرجات', Column(children: [
          SwitchListTile(title: const Text('تفعيل التدرج في المفاتيح'), value: p.useGradient,
            onChanged: (v) => update((p) => p.useGradient = v)),
          Wrap(spacing: 8, children: [
            _gradientChip('Aurora', const [Color(0xFF16D9F2), Color(0xFF8B5CF6)]),
            _gradientChip('Sunset', const [Color(0xFFFF8A65), Color(0xFFEC4899)]),
            _gradientChip('Forest', const [Color(0xFF16A34A), Color(0xFF064E3B)]),
            _gradientChip('Gold', const [Color(0xFFFDE68A), Color(0xFFB45309)]),
          ]),
        ]));
      case 'Shapes':
        return _card('مكتبة الأشكال', Wrap(spacing: 10, runSpacing: 10, children: [
          for (final item in ['Rounded','Pill','Square','Soft','Outlined','Glass'])
            ChoiceChip(label: Text(item), selected: p.shapeName == item, onSelected: (_) => update((p) { p.shapeName = item; p.radius = item == 'Square' ? 3 : item == 'Pill' ? 24 : 12; })),
          _slider('استدارة الحواف', p.radius, 2, 26, (v) => update((p) => p.radius = v)),
          const Text('مجموعة أولية من الأنماط؛ يمكن توسيع المكتبة بإضافة SVG/PNG مرخصة.'),
        ]));
      case 'Backgrounds':
        return _card('الخلفيات', Column(children: [
          SwitchListTile(title: const Text('استخدام صورة خلفية'), value: p.useBackgroundImage, onChanged: (v) => update((p) => p.useBackgroundImage = v)),
          DropdownButtonFormField<String>(value: p.backgroundName, decoration: const InputDecoration(labelText: 'الخلفية المختارة'),
            items: ['Midnight','Woodland','Pet Paws','Sunset','Galaxy','Minimal'].map((e) => DropdownMenuItem(value: e, child: Text(e))).toList(),
            onChanged: (v) => update((p) => p.backgroundName = v ?? 'Midnight')),
          const SizedBox(height: 8),
          const Text('استيراد الصور من الهاتف سيُضاف عبر منتقي الملفات في شاشة الأصول.'),
        ]));
      case 'Fonts':
        return _card('الخطوط', Column(children: [
          DropdownButtonFormField<String>(value: p.fontName, decoration: const InputDecoration(labelText: 'خط المعاينة'),
            items: ['Roboto','Cairo','Tajawal','Amiri','Noto Sans Arabic','Poppins','Montserrat'].map((e) => DropdownMenuItem(value: e, child: Text(e))).toList(),
            onChanged: (v) => update((p) => p.fontName = v ?? 'Roboto')),
          const SizedBox(height: 10),
          const Text('الخطوط التي يتطلب تنزيلها تحتاج اتصالًا بالإنترنت. يلزم تضمين ملف TTF/OTF مناسب داخل ZIP كي يستخدمه FUTO، إذا كان مدعومًا في إعدادات الثيم.'),
        ]));
      case 'Keys':
        return _card('نظام المفاتيح', Column(children: [
          SwitchListTile(title: const Text('إظهار شريط الأدوات'), value: p.showActionRow, onChanged: (v) => update((p) => p.showActionRow = v)),
          SwitchListTile(title: const Text('إظهار شريط الاقتراحات'), value: p.showSuggestionRow, onChanged: (v) => update((p) => p.showSuggestionRow = v)),
          _slider('حجم الحروف', p.keyScale, .7, 1.4, (v) => update((p) => p.keyScale = v)),
          _slider('المسافة بين المفاتيح', p.keyGap, 1, 8, (v) => update((p) => p.keyGap = v)),
        ]));
      case 'Layout':
        return _card('التخطيط', Column(children: [
          DropdownButtonFormField<String>(value: p.layoutName, decoration: const InputDecoration(labelText: 'تخطيط لوحة المفاتيح'),
            items: ['Arabic','English QWERTY','AZERTY','QWERTZ','Compact'].map((e) => DropdownMenuItem(value: e, child: Text(e))).toList(),
            onChanged: (v) => update((p) => p.layoutName = v ?? 'Arabic')),
          const Text('المعاينة الحالية تعرض تخطيطًا عربيًا بصريًا. مطابقة التخطيطات الأخرى تحتاج إلى التحقق مقابل تخطيطات FUTO الفعلية.'),
        ]));
      case 'Metadata':
        return _card('البيانات الوصفية', Column(children: [
          _textField('اسم الثيم', p.name, (v) => update((p) => p.name = v)),
          _textField('المؤلف', p.author, (v) => update((p) => p.author = v)),
          _textField('الوصف', p.description, (v) => update((p) => p.description = v), maxLines: 3),
        ]));
      case 'Matchrules':
        return _card('قواعد المطابقة', const Text('القواعد المبدئية: normal، functional، spacebar، delete_key، enter_key. تُولَّد القواعد مع الأصول المتوافقة في حزمة ZIP. أضف القواعد المتقدمة بعد التحقق من selectors المدعومة رسميًا.'));
      case 'Assets':
        return _card('الأصول', const Text('إدارة PNG وSVG والخطوط والخلفيات. سيضمّن المصدّر الأصول التي تُمرّر إليه ويتحقق من وجودها. أضف ملفاتك إلى المشروع داخل assets/ أو استوردها من الهاتف.'));
      case 'Effects':
        return _card('التأثيرات', const Text('المعاينة تدعم تدرجًا بصريًا وحدودًا. تأثيرات مثل blur وglow والأنيميشن ليست بالضرورة قابلة للتصدير إلى FUTO كإعدادات مباشرة؛ يلزم تحويلها إلى صور أصول إذا كان التنسيق يدعمها.'));
      case 'Animations':
        return _card('الأنيميشن', const Text('إعدادات الحركة مخصصة لواجهة الاستوديو. لا تُصدَّر حركة الزر على أنها ميزة FUTO إلا إذا دعمها التنسيق المستهدف.'));
      case 'Sounds':
        return _card('الأصوات', const Text('يمكن إضافة معاينة صوتية داخل الاستوديو مستقبلًا. حزمة الثيم لا تتضمن أصواتًا إلا إذا كان تنسيق FUTO المستهدف يدعمها.'));
      default:
        return _card(section, const Text('لوحة إعدادات جاهزة للتوسعة مع الحفاظ على نموذج بيانات موحد.'));
    }
  }

  Widget _colorTile(String label, Color color, ValueChanged<Color> apply) => Card(
    child: ListTile(
      leading: Container(width: 34, height: 34, decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(9), border: Border.all(color: Colors.white24))),
      title: Text(label),
      subtitle: Text('#${color.value.toRadixString(16).substring(2).toUpperCase()}'),
      trailing: const Icon(Icons.chevron_right),
      onTap: () => chooseColor(label, color, apply),
    ),
  );

  Widget _gradientChip(String name, List<Color> colors) => InkWell(
    onTap: () => update((p) { p.gradientName = name; p.primary = colors.first; p.secondary = colors.last; p.useGradient = true; }),
    child: Container(width: 78, height: 56, margin: const EdgeInsets.symmetric(vertical: 6),
      decoration: BoxDecoration(gradient: LinearGradient(colors: colors), borderRadius: BorderRadius.circular(12), border: Border.all(color: Colors.white30)),
      child: Align(alignment: Alignment.bottomCenter, child: Container(
        width: double.infinity, padding: const EdgeInsets.all(4),
        decoration: BoxDecoration(color: Colors.black.withValues(alpha: .28), borderRadius: const BorderRadius.vertical(bottom: Radius.circular(12))),
        child: Text(name, textAlign: TextAlign.center, style: const TextStyle(color: Colors.white, fontSize: 10)),
      )),
    ),
  );

  Widget _slider(String label, double value, double min, double max, ValueChanged<double> onChanged) => Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [Text('$label: ${value.toStringAsFixed(1)}'), Slider(value: value.clamp(min, max), min: min, max: max, onChanged: onChanged)],
  );

  Widget _textField(String label, String value, ValueChanged<String> onChanged, {int maxLines = 1}) => Padding(
    padding: const EdgeInsets.symmetric(vertical: 6),
    child: TextFormField(initialValue: value, maxLines: maxLines, decoration: InputDecoration(labelText: label, border: const OutlineInputBorder()),
      onChanged: onChanged),
  );

  Widget _card(String title, Widget child) => Card(
    child: Padding(padding: const EdgeInsets.all(16), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Text(title, style: Theme.of(context).textTheme.titleLarge),
      const SizedBox(height: 12), child,
    ])),
  );
}
