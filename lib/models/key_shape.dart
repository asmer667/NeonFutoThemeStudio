enum KeyShape {
  rectangle,
  roundedRectangle,
  circle,
  hexagon,
  triangle,
  star,
  diamond,
  pentagon,
  octagon,
  heart,
  custom,
}

extension KeyShapeExtension on KeyShape {
  String get label {
    switch (this) {
      case KeyShape.rectangle:
        return 'مستطيل';
      case KeyShape.roundedRectangle:
        return 'مستطيل مستدير';
      case KeyShape.circle:
        return 'دائري';
      case KeyShape.hexagon:
        return 'سداسي';
      case KeyShape.triangle:
        return 'مثلث';
      case KeyShape.star:
        return 'نجمة';
      case KeyShape.diamond:
        return 'معيّن';
      case KeyShape.pentagon:
        return 'خماسي';
      case KeyShape.octagon:
        return 'ثماني';
      case KeyShape.heart:
        return 'قلب';
      case KeyShape.custom:
        return 'مخصص';
    }
  }
}
