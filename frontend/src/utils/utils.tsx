import _ from "lodash";

function keysToCamel(o: any): any {
  if (Array.isArray(o)) {
    return o.map(v => keysToCamel(v));
  } else if (o !== null && o.constructor === Object) {
    return Object.fromEntries(
      Object.entries(o).map(([k, v]) => [_.camelCase(k), keysToCamel(v)])
    );
  }
  return o;
}

export default keysToCamel;