export class BlobUtils {
  static toString(blob: Blob, encoding?: string): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        resolve(reader.result as string);
      };
      reader.readAsText(blob, encoding);
    });
  }
}
