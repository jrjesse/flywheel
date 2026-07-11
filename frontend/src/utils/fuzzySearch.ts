export function getBigrams(str: string): string[] {
  const bigrams: string[] = [];
  for (let i = 0; i < str.length - 1; i++) {
    bigrams.push(str.slice(i, i + 2));
  }
  return bigrams;
}

export function bigramSimilarity(str1: string, str2: string): number {
  if (!str1 || !str2) return 0;
  
  const s1 = str1.toLowerCase().trim();
  const s2 = str2.toLowerCase().trim();
  
  if (s1 === s2) return 1;
  if (s1.includes(s2) || s2.includes(s1)) return 0.8; // High score for substring match
  if (s1.length < 2 || s2.length < 2) return 0;
  
  const bg1 = getBigrams(s1);
  const bg2 = getBigrams(s2);
  const bg2Length = bg2.length;
  
  let matches = 0;
  for (const bigram of bg1) {
    const index = bg2.indexOf(bigram);
    if (index > -1) {
      matches++;
      bg2.splice(index, 1);
    }
  }
  
  return (2.0 * matches) / (bg1.length + bg2Length);
}
