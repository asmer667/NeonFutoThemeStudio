import 'dart:typed_data';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import '../models/theme_project.dart';
import '../services/project_store.dart';
import '../services/theme_export_service.dart';
import '../widgets/keyboard_preview.dart';
import 'editor_screen.dart';

class StudioShell extends StatefulWidget {
  const StudioShell({super.key});
  @override
  State<StudioShell> createState() => _StudioShellState();
}

class _StudioShellState extends State<StudioShell> {
  final ProjectStore store = ProjectStore();
  final ThemeExportService exporter = ThemeExportService();
  late ThemeProject project;
  List<ThemeProject> projects = [];
  int selected = 0;
  bool dark = true;
  String currentScreen = 'Home';

  final screens = const [
    ('Home', Icons.dashboard_outlined, 'الرئيسية'),
    ('Editor', Icons.tune, 'محرر الثيم'),
    ('Preview', Icons.keyboard_alt_outlined, 'المعاينة'),
    ('Colors', Icons.palette_outlined, 'الألوان'),
    ('Gradients', Icons.gradient, 'التدرجات'),
    ('Shapes', Icons.category_outlined, 'الأشكال'),
    ('Backgrounds', Icons.wallpaper_outlined, 'الخلفيات'),
    ('Fonts', Icons.text_fields, 'الخطوط'),
    ('Assets', Icons.folder_copy_outlined, 'الأصول'),
    ('Matchrules', Icons.rule, 'قواعد المطابقة'),
    ('Keys', Icons.grid_view, 'المفاتيح'),
    ('Effects', Icons.auto_awesome, 'التأثيرات'),
    ('Animations', Icons.animation, 'الأنيميشن'),
    ('Sounds', Icons.volume_up_outlined, 'الأصوات'),
    ('Layout', Icons.view_quilt_outlined, 'التخطيط'),
    ('Metadata', Icons.badge_outlined, 'البيانات الوصفية'),
    ('Import', Icons.file_open_outlined, 'استيراد'),
    ('Export', Icons.archive_outlined, 'تصدير ZIP'),
    ('Saved', Icons.bookmarks_outlined, 'ثيماتي'),
    ('Settings', Icons.settings_outlined, 'الإعدادات'),
    ('Help', Icons.help_outline, 'المساعدة'),
    ('About', Icons.info_outline, 'حول التطبيق'),
  ];

  @override
  void initState() {
    super.initState();
    project = ThemeProject();
    _load();
  }

  Future<void> _load() async {
    final loaded = await store.loadAll();
    if (!mounted) return;
    setState(() { projects = loaded; project = loaded.first; });
  }

  Future<void> _save() async {
    final existing = projects.indexWhere((p) => p.id == project.id);
    if (existing >= 0) {
      projects[existing] = project.copy();
    } else {
      projects.insert(0, project.copy());
    }
    await store.saveAll(projects);
    if (mounted) ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('تم حفظ الثيم على الجهاز')));
  }

  Future<void> _export() async {
    try {
      final bytes = await exporter.createZip(project);
      final result = await FilePicker.platform.saveFile(
        dialogTitle: 'حفظ حزمة الثيم',
        fileName: '${_safeName(project.name)}.zip',
        type: FileType.custom,
        allowedExtensions: ['zip'],
        bytes: bytes,
      );
      if (mounted && result != null) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('تم إنشاء الحزمة: $result')));
      }
    } catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('تعذر التصدير: $e')));
    }
  }

  String _safeName(String name) => name.replaceAll(RegExp(r'[^a-zA-Z0-9_-]+'), '_');

  Future<void> _import() async {
    final result = await FilePicker.platform.pickFiles(type: FileType.custom, allowedExtensions: ['zip','json'], withData: true);
    if (result == null || result.files.isEmpty) return;
    final file = result.files.single;
    if (file.name.toLowerCase().endsWith('.json') && file.bytes != null) {
      try {
        final decoded = ThemeProject.fromJsonString(String.fromCharCodes(file.bytes!));
        setState(() => project = decoded);
        await _save();
        return;
      } catch (_) {}
    }
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(
        'تم اختيار ${file.name}. فحص ZIP المتقدم وتعبئة نموذج الثيم المستورد يحتاج إلى مطابقة ملف theme.txt مع مخطط FUTO الحالي.',
      )));
    }
  }

  void _select(String id) {
    setState(() {
      currentScreen = id;
      if (id == 'Colors' || id == 'Gradients' || id == 'Shapes' || id == 'Backgrounds' ||
          id == 'Fonts' || id == 'Assets' || id == 'Matchrules' || id == 'Keys' ||
          id == 'Effects' || id == 'Animations' || id == 'Sounds' || id == 'Layout' || id == 'Metadata') {
        currentScreen = 'Editor';
      }
    });
    if (id == 'Export') _export();
    if (id == 'Import') _import();
  }

  @override
  Widget build(BuildContext context) {
    final scheme = ColorScheme.fromSeed(seedColor: project.primary, brightness: dark ? Brightness.dark : Brightness.light);
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData(useMaterial3: true, colorScheme: scheme, scaffoldBackgroundColor: dark ? const Color(0xFF07111F) : const Color(0xFFF4F6FA)),
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Ultimate Theme Creator Studio', maxLines: 1, overflow: TextOverflow.ellipsis),
          actions: [
            IconButton(tooltip: 'حفظ', onPressed: _save, icon: const Icon(Icons.save_outlined)),
            IconButton(tooltip: 'تصدير ZIP', onPressed: _export, icon: const Icon(Icons.archive_outlined)),
            IconButton(tooltip: 'الوضع الفاتح/الداكن', onPressed: () => setState(() => dark = !dark), icon: Icon(dark ? Icons.light_mode : Icons.dark_mode)),
          ],
        ),
        body: LayoutBuilder(builder: (context, constraints) {
          final wide = constraints.maxWidth > 850;
          return Row(children: [
            if (wide) SizedBox(width: 230, child: _navigationRail()),
            Expanded(child: _screenBody()),
          ]);
        }),
        bottomNavigationBar: MediaQuery.sizeOf(context).width <= 850
            ? NavigationBar(selectedIndex: _bottomIndex(), onDestinationSelected: (i) => _select(['Home','Editor','Preview','Saved','Settings'][i]),
                destinations: const [
                  NavigationDestination(icon: Icon(Icons.home_outlined), label: 'الرئيسية'),
                  NavigationDestination(icon: Icon(Icons.tune), label: 'المحرر'),
                  NavigationDestination(icon: Icon(Icons.keyboard_alt_outlined), label: 'معاينة'),
                  NavigationDestination(icon: Icon(Icons.bookmarks_outlined), label: 'ثيماتي'),
                  NavigationDestination(icon: Icon(Icons.settings_outlined), label: 'إعدادات'),
                ])
            : null,
      ),
    );
  }

  int _bottomIndex() {
    final ids = ['Home','Editor','Preview','Saved','Settings'];
    final i = ids.indexOf(currentScreen);
    return i < 0 ? 0 : i;
  }

  Widget _navigationRail() => ListView(
    padding: const EdgeInsets.symmetric(vertical: 10),
    children: [
      const Padding(padding: EdgeInsets.all(16), child: Text('STUDIO • v0.1.0', style: TextStyle(fontWeight: FontWeight.bold, letterSpacing: 1.2))),
      for (final item in screens)
        ListTile(
          dense: true,
          selected: currentScreen == item.$1 || (currentScreen == 'Editor' && ['Colors','Gradients','Shapes','Backgrounds','Fonts','Assets','Matchrules','Keys','Effects','Animations','Sounds','Layout','Metadata'].contains(item.$1)),
          leading: Icon(item.$2, size: 20),
          title: Text(item.$3),
          onTap: () => _select(item.$1),
        ),
    ],
  );

  Widget _screenBody() {
    switch (currentScreen) {
      case 'Editor':
        return EditorScreen(project: project, onChanged: (p) => setState(() => project = p));
      case 'Preview':
        return ListView(padding: const EdgeInsets.all(16), children: [
          Text('معاينة مباشرة', style: Theme.of(context).textTheme.headlineSmall),
          const SizedBox(height: 12),
          KeyboardPreview(project: project),
          const SizedBox(height: 16),
          const Text('هذه معاينة مرسومة في Flutter وفق نموذج الثيم؛ قد تختلف بعض التفاصيل عن محرك FUTO الحقيقي.'),
        ]);
      case 'Saved':
        return _savedScreen();
      case 'Settings':
        return _settingsScreen();
      case 'Import':
        return _info('استيراد الثيمات', 'اختر ZIP أو JSON. الاستيراد الكامل لـ ZIP يحتاج محللًا يقرأ TOML وأصول FUTO ويتحقق منها قبل تعبئة جميع خصائص المحرر.');
      case 'Export':
        return _info('تصدير ZIP', 'المصدّر يولّد theme.txt وحزمة ZIP. اختبر الحزمة داخل FUTO Keyboard قبل اعتبارها متوافقة نهائيًا.');
      case 'Help':
        return _info('المساعدة', 'ابدأ بثيم محفوظ، عدّل الألوان والأشكال، شاهد المعاينة، ثم صدّر ZIP واستورده في FUTO Keyboard للتحقق.');
      case 'About':
        return _info('حول التطبيق', 'Ultimate Theme Creator Studio — Flutter/Dart foundation for FUTO Keyboard theme authoring.');
      default:
        return _homeScreen();
    }
  }

  Widget _homeScreen() => ListView(
    padding: const EdgeInsets.all(16),
    children: [
      Container(
        padding: const EdgeInsets.all(22),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(24),
          gradient: LinearGradient(colors: [project.primary.withValues(alpha: .24), project.secondary.withValues(alpha: .20), const Color(0xFF112D49)]),
          border: Border.all(color: project.primary.withValues(alpha: .45)),
        ),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          const Text('CREATE • CUSTOMIZE • EXPORT', style: TextStyle(letterSpacing: 1.4, fontSize: 11)),
          const SizedBox(height: 8),
          Text('صمّم ثيمك بطريقتك', style: Theme.of(context).textTheme.headlineMedium?.copyWith(fontWeight: FontWeight.w800)),
          const SizedBox(height: 6),
          const Text('محرر ثيمات متكامل مع معاينة عربية وتصدير حزم FUTO.'),
          const SizedBox(height: 16),
          Wrap(spacing: 8, runSpacing: 8, children: [
            FilledButton.icon(onPressed: () => setState(() => currentScreen = 'Editor'), icon: const Icon(Icons.add), label: const Text('إنشاء / تحرير')),
            OutlinedButton.icon(onPressed: _import, icon: const Icon(Icons.file_open), label: const Text('استيراد')),
            OutlinedButton.icon(onPressed: _export, icon: const Icon(Icons.archive), label: const Text('تصدير ZIP')),
          ]),
        ]),
      ),
      const SizedBox(height: 18),
      Row(children: [
        Expanded(child: _stat('المشاريع', '${projects.length}', Icons.folder_copy_outlined)),
        const SizedBox(width: 10),
        Expanded(child: _stat('الألوان', '7 قابلة للتحرير', Icons.palette_outlined)),
        const SizedBox(width: 10),
        Expanded(child: _stat('التنسيق', 'FUTO ZIP', Icons.inventory_2_outlined)),
      ]),
      const SizedBox(height: 20),
      Text('معاينة الثيم الحالي', style: Theme.of(context).textTheme.titleLarge),
      const SizedBox(height: 10),
      KeyboardPreview(project: project, compact: true),
      const SizedBox(height: 20),
      Text('ابدأ من مجموعة جاهزة', style: Theme.of(context).textTheme.titleLarge),
      const SizedBox(height: 10),
      GridView.count(
        crossAxisCount: MediaQuery.sizeOf(context).width > 700 ? 3 : 2,
        shrinkWrap: true, physics: const NeverScrollableScrollPhysics(), crossAxisSpacing: 10, mainAxisSpacing: 10, childAspectRatio: 1.25,
        children: [
          _preset('Aurora Glass', const Color(0xFF16D9F2), const Color(0xFF8B5CF6)),
          _preset('Neon Cyber', const Color(0xFF00D9FF), const Color(0xFF0D2C48)),
          _preset('Forest Nature', const Color(0xFF86EFAC), const Color(0xFF064E3B)),
          _preset('Sunset Dream', const Color(0xFFFFA36C), const Color(0xFFEC4899)),
          _preset('Gold Luxury', const Color(0xFFFDE68A), const Color(0xFF92400E)),
          _preset('Pet Keys', const Color(0xFF9BAF91), const Color(0xFFFFE9C7)),
        ],
      ),
    ],
  );

  Widget _preset(String name, Color a, Color b) => InkWell(
    onTap: () => setState(() { project.name = name; project.primary = a; project.secondary = b; project.useGradient = true; }),
    borderRadius: BorderRadius.circular(16),
    child: Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(gradient: LinearGradient(colors: [a.withValues(alpha: .24), b.withValues(alpha: .38)]), borderRadius: BorderRadius.circular(16), border: Border.all(color: a.withValues(alpha: .6))),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
        Row(children: [CircleAvatar(radius: 7, backgroundColor: a), const SizedBox(width: 5), CircleAvatar(radius: 7, backgroundColor: b)]),
        Text(name, style: const TextStyle(fontWeight: FontWeight.bold)),
        const Align(alignment: Alignment.bottomRight, child: Icon(Icons.arrow_outward, size: 18)),
      ]),
    ),
  );

  Widget _stat(String label, String value, IconData icon) => Card(child: Padding(
    padding: const EdgeInsets.all(12),
    child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Icon(icon, color: project.primary), const SizedBox(height: 8), Text(value, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)), Text(label, style: const TextStyle(fontSize: 11, color: Colors.white60)),
    ]),
  ));

  Widget _savedScreen() => ListView(padding: const EdgeInsets.all(16), children: [
    Text('ثيماتي المحفوظة', style: Theme.of(context).textTheme.headlineSmall),
    const SizedBox(height: 12),
    FilledButton.icon(onPressed: _save, icon: const Icon(Icons.save), label: const Text('حفظ الثيم الحالي')),
    for (final saved in projects)
      Card(child: ListTile(
        leading: CircleAvatar(backgroundColor: saved.primary, child: Icon(Icons.palette, color: saved.keyText)),
        title: Text(saved.name), subtitle: Text(saved.author),
        trailing: IconButton(icon: const Icon(Icons.edit), onPressed: () => setState(() { project = saved.copy(); currentScreen = 'Editor'; })),
      )),
  ]);

  Widget _settingsScreen() => ListView(padding: const EdgeInsets.all(16), children: [
    Text('الإعدادات', style: Theme.of(context).textTheme.headlineSmall),
    SwitchListTile(title: const Text('الوضع الداكن'), value: dark, onChanged: (v) => setState(() => dark = v)),
    ListTile(leading: const Icon(Icons.language), title: const Text('اللغة'), subtitle: const Text('واجهة عربية — دعم الإنجليزية ضمن خطة التوسعة')),
    ListTile(leading: const Icon(Icons.storage), title: const Text('التخزين'), subtitle: const Text('المشاريع محفوظة محليًا على الجهاز')),
    ListTile(leading: const Icon(Icons.security), title: const Text('الخصوصية'), subtitle: const Text('لا يتطلب المشروع الحالي حسابًا سحابيًا')),
  ]);

  Widget _info(String title, String description) => ListView(padding: const EdgeInsets.all(16), children: [
    Text(title, style: Theme.of(context).textTheme.headlineSmall),
    const SizedBox(height: 12),
    Card(child: Padding(padding: const EdgeInsets.all(16), child: Text(description))),
  ]);
}

