import 'dart:io';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';
import '../models/theme_project.dart';
import '../models/key_shape.dart';
import '../services/theme_export_service.dart';
import '../services/asset_generator.dart';
import '../widgets/keyboard_preview.dart';

class EditorScreen extends StatefulWidget {
  const EditorScreen({super.key});

  @override
  State<EditorScreen> createState() => _EditorScreenState();
}

class _EditorScreenState extends State<EditorScreen> {
  late ThemeProject _project;
  KeyShape _selectedShape = KeyShape.roundedRectangle;
  final _nameController = TextEditingController();
  bool _exporting = false;

  @override
  void initState() {
    super.initState();
    _project = ThemeProject.createDefault();
    _nameController.text = _project.name;
  }

  @override
  void dispose() {
    _nameController.dispose();
    super.dispose();
  }

  Future<void> _pickColor(String field) async {
    Color current;
    switch (field) {
      case 'primary': current = _project.primary; break;
      case 'secondary': current = _project.secondary; break;
      case 'surface': current = _project.surface; break;
      case 'keyboardSurface': current = _project.keyboardSurface; break;
      case 'keyboardContainer': current = _project.keyboardContainer; break;
      case 'keyboardPress': current = _project.keyboardPress; break;
      default: return;
    }

    final picked = await showDialog<Color>(
      context: context,
      builder: (_) => _ColorPickerDialog(initial: current),
    );

    if (picked != null) {
      setState(() {
        switch (field) {
          case 'primary': _project.primary = picked; break;
          case 'secondary': _project.secondary = picked; break;
          case 'surface':
            _project.surface = picked;
            _project.keyboardSurface = picked;
            break;
          case 'keyboardSurface': _project.keyboardSurface = picked; break;
          case 'keyboardContainer': _project.keyboardContainer = picked; break;
          case 'keyboardPress': _project.keyboardPress = picked; break;
        }
      });
    }
  }

  Future<void> _exportTheme() async {
    setState(() => _exporting = true);
    try {
      final dir = await getTemporaryDirectory();
      final assetsDir = Directory('${dir.path}/futo_assets');
      if (!await assetsDir.exists()) {
        await assetsDir.create(recursive: true);
      }

      // توليد الأزرار
      final assets = <File>[];

      final btnDefault = await AssetGenerator.generateButtonPng(
        bgColor: _project.keyboardContainer,
        borderColor: _project.surface,
      );
      assets.add(await AssetGenerator.writePng('Button-default.png', btnDefault, assetsDir));

      final btnFunction = await AssetGenerator.generateButtonPng(
        bgColor: ThemeExportService.adjustBrightness(_project.keyboardContainer, 0.05).let((h) => _hexToColor(h)),
        borderColor: _project.surface,
      );
      assets.add(await AssetGenerator.writePng('Button-function.png', btnFunction, assetsDir));

      final btnSpace = await AssetGenerator.generateButtonPng(
        bgColor: _project.keyboardPress,
        borderColor: _project.surface,
      );
      assets.add(await AssetGenerator.writePng('Button-space.png', btnSpace, assetsDir));

      final iconBack = await AssetGenerator.generateIconPng(
        icon: Icons.backspace_outlined,
      );
      assets.add(await AssetGenerator.writePng('Icon-backspace.png', iconBack, assetsDir));

      final iconEnter = await AssetGenerator.generateIconPng(
        icon: Icons.keyboard_return,
      );
      assets.add(await AssetGenerator.writePng('Icon-enter.png', iconEnter, assetsDir));

      // توليد ZIP
      final file = await ThemeExportService.exportThemeZip(_project, assets, dir);

      if (!mounted) return;
      await Share.shareXFiles([XFile(file.path)], text: 'FUTO Theme: ${_project.name}');
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('خطأ في التصدير: $e')),
      );
    } finally {
      if (mounted) setState(() => _exporting = false);
    }
  }

  Color _hexToColor(String hex) {
    final h = hex.replaceAll('#', '');
    return Color(int.parse(h, radix: 16));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('FUTO Theme Studio'),
        actions: [
          if (_exporting)
            const Padding(
              padding: EdgeInsets.all(16),
              child: SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(strokeWidth: 2),
              ),
            )
          else
            IconButton(
              icon: const Icon(Icons.save_alt),
              tooltip: 'تصدير ZIP',
              onPressed: _exportTheme,
            ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // اسم الثيم
          TextField(
            controller: _nameController,
            decoration: const InputDecoration(
              labelText: 'اسم الثيم',
              border: OutlineInputBorder(),
            ),
            onChanged: (v) => setState(() => _project.name = v),
          ),
          const SizedBox(height: 20),

          // المعاينة
          const Text('المعاينة الحية', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          KeyboardPreview(project: _project, shape: _selectedShape),
          const SizedBox(height: 20),

          // اختيار شكل الأزرار
          const Text('شكل الأزرار', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: KeyShape.values.map((s) {
              final selected = s == _selectedShape;
              return ChoiceChip(
                label: Text(s.label),
                selected: selected,
                onSelected: (_) => setState(() => _selectedShape = s),
              );
            }).toList(),
          ),
          const SizedBox(height: 20),

          // محددات الألوان
          const Text('الألوان', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          _colorTile('اللون الأساسي (Primary)', _project.primary, () => _pickColor('primary')),
          _colorTile('اللون الثانوي (Secondary)', _project.secondary, () => _pickColor('secondary')),
          _colorTile('السطح (Surface)', _project.surface, () => _pickColor('surface')),
          _colorTile('خلفية المفاتيح (Keyboard Container)', _project.keyboardContainer, () => _pickColor('keyboardContainer')),
          _colorTile('لون الضغط (Keyboard Press)', _project.keyboardPress, () => _pickColor('keyboardPress')),
          const SizedBox(height: 20),

          // استدارة المفاتيح
          Text('استدارة المفاتيح: ${(_project.roundedness * 100).toStringAsFixed(0)}%'),
          Slider(
            value: _project.roundedness,
            onChanged: (v) => setState(() => _project.roundedness = v),
          ),
          const SizedBox(height: 20),

          // زر التصدير
          ElevatedButton.icon(
            onPressed: _exporting ? null : _exportTheme,
            icon: const Icon(Icons.download),
            label: const Text('تصدير الثيم كـ ZIP'),
            style: ElevatedButton.styleFrom(minimumSize: const Size.fromHeight(56)),
          ),
        ],
      ),
    );
  }

  Widget _colorTile(String title, Color color, VoidCallback onTap) {
    return ListTile(
      title: Text(title),
      trailing: Container(
        width: 48,
        height: 48,
        decoration: BoxDecoration(
          color: color,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: Colors.white30, width: 2),
        ),
      ),
      onTap: onTap,
    );
  }
}

extension _Let<T> on T {
  R let<R>(R Function(T) block) => block(this);
}

class _ColorPickerDialog extends StatefulWidget {
  final Color initial;
  const _ColorPickerDialog({required this.initial});

  @override
  State<_ColorPickerDialog> createState() => _ColorPickerDialogState();
}

class _ColorPickerDialogState extends State<_ColorPickerDialog> {
  late double r, g, b;

  @override
  void initState() {
    super.initState();
    r = widget.initial.r * 255;
    g = widget.initial.g * 255;
    b = widget.initial.b * 255;
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('اختر لوناً'),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              height: 60,
              decoration: BoxDecoration(
                color: Color.fromRGBO(r.round(), g.round(), b.round(), 1),
                borderRadius: BorderRadius.circular(8),
              ),
            ),
            const SizedBox(height: 16),
            _slider('R', r, (v) => setState(() => r = v)),
            _slider('G', g, (v) => setState(() => g = v)),
            _slider('B', b, (v) => setState(() => b = v)),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('إلغاء'),
        ),
        ElevatedButton(
          onPressed: () => Navigator.pop(
            context,
            Color.fromRGBO(r.round(), g.round(), b.round(), 1),
          ),
          child: const Text('موافق'),
        ),
      ],
    );
  }

  Widget _slider(String label, double value, ValueChanged<double> onChange) {
    return Row(
      children: [
        SizedBox(width: 20, child: Text(label)),
        Expanded(
          child: Slider(value: value, min: 0, max: 255, onChanged: onChange),
        ),
        SizedBox(width: 40, child: Text(value.round().toString())),
      ],
    );
  }
}
