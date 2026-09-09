import type {
  NormativeLevel1NodeDto,
  NormativeLevel2NodeDto,
  NormativeLevel3NodeDto,
} from '../../../api/model';

export interface NormativeIndicatorPath {
  level1Id: string;
  level2Id: string;
  level3Id: string;
  indicatorId: string;
}

export function findNormativeIndicatorPath(
  level1Nodes: NormativeLevel1NodeDto[],
  indicatorId: string,
): NormativeIndicatorPath | null {
  for (const level1 of level1Nodes) {
    const level1Id = level1.id;
    if (!level1Id) continue;

    for (const level2 of level1.level2Nodes ?? []) {
      const level2Id = level2.id;
      if (!level2Id) continue;

      for (const level3 of level2.level3Nodes ?? []) {
        const level3Id = level3.id;
        if (!level3Id) continue;

        const found = (level3.indicators ?? []).some((indicator) => indicator.id === indicatorId);
        if (found) {
          return { level1Id, level2Id, level3Id, indicatorId };
        }
      }
    }
  }
  return null;
}

export function countIndicatorsUnderLevel1(node: NormativeLevel1NodeDto): number {
  return (node.level2Nodes ?? []).reduce(
    (sum, level2) => sum + countIndicatorsUnderLevel2(level2),
    0,
  );
}

export function countIndicatorsUnderLevel2(node: NormativeLevel2NodeDto): number {
  return (node.level3Nodes ?? []).reduce(
    (sum, level3) => sum + (level3.indicators?.length ?? 0),
    0,
  );
}

export function countIndicatorsUnderLevel3(node: NormativeLevel3NodeDto): number {
  return node.indicators?.length ?? 0;
}

export function sortByOrder<T extends { order?: number }>(items: T[]): T[] {
  return [...items].sort((a, b) => (a.order ?? 0) - (b.order ?? 0));
}
