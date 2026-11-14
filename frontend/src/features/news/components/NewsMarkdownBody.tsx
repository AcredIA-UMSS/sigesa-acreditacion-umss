import type { ReactNode } from 'react';

type NewsMarkdownBodyProps = {
  body: string;
};

function renderInline(text: string): ReactNode[] {
  const nodes: ReactNode[] = [];
  const pattern = /(\*\*[^*]+\*\*|\*[^*]+\*)/g;
  let last = 0;
  let match = pattern.exec(text);
  let key = 0;
  while (match != null) {
    if (match.index > last) {
      nodes.push(text.slice(last, match.index));
    }
    const token = match[0];
    if (token.startsWith('**') && token.endsWith('**')) {
      nodes.push(<strong key={`b-${key}`}>{token.slice(2, -2)}</strong>);
    } else {
      nodes.push(<em key={`i-${key}`}>{token.slice(1, -1)}</em>);
    }
    key += 1;
    last = match.index + token.length;
    match = pattern.exec(text);
  }
  if (last < text.length) {
    nodes.push(text.slice(last));
  }
  return nodes;
}

export function NewsMarkdownBody({ body }: NewsMarkdownBodyProps) {
  const blocks = body.replace(/\r\n/g, '\n').split(/\n{2,}/);

  return (
    <div className="space-y-3 text-body-lg text-gray-800">
      {blocks.map((block, index) => {
        const trimmed = block.trim();
        if (trimmed.length === 0) {
          return null;
        }
        if (trimmed.startsWith('### ')) {
          return (
            <h4 key={index} className="text-heading-sm text-primary-800">
              {renderInline(trimmed.slice(4))}
            </h4>
          );
        }
        if (trimmed.startsWith('## ')) {
          return (
            <h3 key={index} className="text-heading-md text-primary-800">
              {renderInline(trimmed.slice(3))}
            </h3>
          );
        }
        if (trimmed.startsWith('# ')) {
          return (
            <h2 key={index} className="text-heading-lg text-primary-800">
              {renderInline(trimmed.slice(2))}
            </h2>
          );
        }
        const lines = trimmed.split('\n');
        if (lines.every((line) => line.trim().startsWith('- '))) {
          return (
            <ul key={index} className="list-disc space-y-1 pl-5">
              {lines.map((line, lineIndex) => (
                <li key={lineIndex}>{renderInline(line.trim().slice(2))}</li>
              ))}
            </ul>
          );
        }
        return (
          <p key={index} className="whitespace-pre-wrap">
            {renderInline(trimmed)}
          </p>
        );
      })}
    </div>
  );
}
