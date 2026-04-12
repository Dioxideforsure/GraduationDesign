/**
 * 文件大小友好显示（字节 → B / KB / MB / GB）
 */
function size2Str(bytes) {
  if (bytes === null || bytes === undefined || bytes === "") {
    return "-";
  }
  const n = Number(bytes);
  if (Number.isNaN(n)) {
    return "-";
  }
  if (n === 0) {
    return "0 B";
  }
  const units = ["B", "KB", "MB", "GB", "TB"];
  let i = 0;
  let v = n;
  while (v >= 1024 && i < units.length - 1) {
    v /= 1024;
    i++;
  }
  const fixed = i === 0 ? 0 : v < 10 ? 2 : v < 100 ? 2 : 1;
  return `${v.toFixed(fixed)} ${units[i]}`;
}

export default {
  size2Str,
};
