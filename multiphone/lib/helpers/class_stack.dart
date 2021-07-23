import 'dart:collection';

class ClassStack<T> {
  final _stack = Queue<T>();

  int get length => _stack.length;

  bool canPop() => _stack.isNotEmpty;

  void clear() {
    while (_stack.isNotEmpty) {
      _stack.removeLast();
    }
  }

  get isEmpty {
    return _stack.isEmpty;
  }

  T get(int index) {
    return _stack.elementAt(index);
  }

  void push(T element) {
    _stack.addLast(element);
  }

  T pop() {
    T lastElement = _stack.last;
    _stack.removeLast();
    return lastElement;
  }

  T peek() => _stack.last;
}
