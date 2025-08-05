import { renderToStaticMarkup } from 'react-dom/server';
import ArtistList, { EnrichableArtistObject } from '../components/ArtistList'

const ExportPage = ({ enrichableArtistObjects }: { enrichableArtistObjects: EnrichableArtistObject[] }) => (
  <html>
    <head>
      <meta charSet="utf-8" />
      <title>Exported Items</title>
      <style>{`body { font-family: sans-serif; }`}</style>
    </head>
    <body>
      <h1>Exported Items</h1>
      <ArtistList enrichableArtistObjects={enrichableArtistObjects} />
    </body>
  </html>
);

export function exportReactHtml(enrichableArtistObjects: EnrichableArtistObject[]) {
  const htmlString = '<!DOCTYPE html>' + renderToStaticMarkup(<ExportPage enrichableArtistObjects={enrichableArtistObjects} />);
  const blob = new Blob([htmlString], { type: 'text/html' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'exported-items.html';
  a.click();
  URL.revokeObjectURL(url);
};

