export class PathUtils {
  static parent(path: string): string {
    path = this.rstrip(path);
    const lastIx = path.lastIndexOf('/');
    if (lastIx < 0) {
      return '/';
    }

    return path.substring(0, lastIx + 1);
  }

  static filename(path: string) {
    path = this.rstrip(path);
    const lastIx = path.lastIndexOf('/');
    if (lastIx < 0) {
      return path;
    }

    return path.substring(lastIx + 1);
  }

  static join(path1: string, path2: string): string {
    return this.rstrip(path1) + '/' + this.lstrip(path2);
  }

  static rstrip(path: string) {
    while (path.endsWith('/')) {
      path = path.substring(0, path.length - 1);
    }
    return path;
  }

  static lstrip(path: string) {
    while (path.startsWith('/')) {
      path = path.substring(1);
    }
    return path;
  }
}
