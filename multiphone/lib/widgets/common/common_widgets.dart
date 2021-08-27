import 'package:flutter/material.dart';
import 'package:flutter_svg/svg.dart';
import 'package:multiphone/helpers/values.dart';

class TextWidget extends StatelessWidget {
  final String text;
  final bool isOnBackground;
  final bool isLimitOverflow;
  final bool isBold;
  final TextAlign textAlign;
  const TextWidget(
    this.text, {
    this.isOnBackground = false,
    this.isLimitOverflow = false,
    this.isBold = false,
    this.textAlign,
    Key key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Text(
      text,
      maxLines: isLimitOverflow ? 1 : null,
      overflow: isLimitOverflow ? TextOverflow.fade : null,
      textAlign: textAlign,
      style: isOnBackground ? theme.accentTextTheme.button : null,
    );
  }
}

class TextHeadingWidget extends StatelessWidget {
  final String text;
  final bool isOnBackground;
  final TextAlign textAlign;
  const TextHeadingWidget(
    this.text, {
    this.isOnBackground = false,
    this.textAlign,
    Key key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Text(
      text,
      textAlign: textAlign,
      style: theme.textTheme.headline6.merge(
        TextStyle(
          color: isOnBackground
              ? theme.accentTextTheme.button.decorationColor
              : null,
        ),
      ),
    );
  }
}

class TextSubheadingWidget extends StatelessWidget {
  final String text;
  final bool isOnBackground;
  final bool isLimitOverflow;
  final TextAlign textAlign;
  const TextSubheadingWidget(
    this.text, {
    this.isOnBackground = false,
    this.isLimitOverflow = false,
    this.textAlign,
    Key key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Text(
      text,
      textAlign: textAlign,
      maxLines: isLimitOverflow ? 1 : null,
      overflow: isLimitOverflow ? TextOverflow.fade : null,
      style: theme.textTheme.subtitle1.merge(
        TextStyle(
            color: isOnBackground
                ? theme.accentTextTheme.button.decorationColor
                : null,
            fontWeight: FontWeight.bold),
      ),
    );
  }
}

class IconWidget extends StatelessWidget {
  final IconData icon;
  final bool isOnBackground;
  final double size;
  const IconWidget(this.icon,
      {this.isOnBackground = false, this.size = Values.image_large, Key key})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Icon(
      icon,
      size: size,
      color: isOnBackground ? theme.accentIconTheme.color : null,
    );
  }
}

class IconSvgWidget extends StatelessWidget {
  final String iconName;
  final bool isOnBackground;
  final double size;
  const IconSvgWidget(this.iconName,
      {this.isOnBackground = false, this.size = Values.image_large, Key key})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return SvgPicture.asset(
      '/images/svg/$iconName.svg',
      width: size,
      height: size,
      color: isOnBackground ? theme.accentIconTheme.color : null,
    );
  }
}
