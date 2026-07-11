export function validateCPF(cpf: string): boolean {
  cpf = cpf.replace(/[^\d]/g, '');
  if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) return false;
  let sum = 0;
  for (let i = 0; i < 9; i++) sum += parseInt(cpf.charAt(i)) * (10 - i);
  let rev = 11 - (sum % 11);
  if (rev === 10 || rev === 11) rev = 0;
  if (rev !== parseInt(cpf.charAt(9))) return false;
  sum = 0;
  for (let i = 0; i < 10; i++) sum += parseInt(cpf.charAt(i)) * (11 - i);
  rev = 11 - (sum % 11);
  if (rev === 10 || rev === 11) rev = 0;
  return rev === parseInt(cpf.charAt(10));
}

export function validateCNPJ(cnpj: string): boolean {
  cnpj = cnpj.replace(/[^a-zA-Z0-9]/g, '').toUpperCase();
  if (cnpj.length !== 14) return false;
  if (!/^[A-Z0-9]{12}\d{2}$/.test(cnpj)) return false;

  const charValue = (c: string) => {
    const code = c.charCodeAt(0);
    if (code >= 48 && code <= 57) return code - 48;
    if (code >= 65 && code <= 90) return code - 55;
    return 0;
  };

  const calcDigit = (base: string, weights: number[]) => {
    let sum = 0;
    for (let i = 0; i < weights.length; i++) {
      sum += charValue(base.charAt(i)) * weights[i];
    }
    const remainder = sum % 11;
    return remainder < 2 ? 0 : 11 - remainder;
  };

  const base12 = cnpj.substring(0, 12);
  const digit1 = calcDigit(base12, [5,4,3,2,9,8,7,6,5,4,3,2]);
  
  if (digit1.toString() !== cnpj.charAt(12)) return false;
  
  const base13 = cnpj.substring(0, 13);
  const digit2 = calcDigit(base13, [6,5,4,3,2,9,8,7,6,5,4,3,2]);
  
  return digit2.toString() === cnpj.charAt(13);
}

export function applyDocumentMask(value: string, type: 'CPF' | 'CNPJ'): string {
  if (type === 'CPF') {
    let v = value.replace(/\D/g, '').substring(0, 11);
    v = v.replace(/(\d{3})(\d)/, '$1.$2');
    v = v.replace(/(\d{3})(\d)/, '$1.$2');
    v = v.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    return v;
  } else {
    let v = value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase().substring(0, 14);
    if (v.length > 2) v = v.substring(0, 2) + '.' + v.substring(2);
    if (v.length > 5) v = v.substring(0, 6) + '.' + v.substring(6);
    if (v.length > 9) v = v.substring(0, 10) + '/' + v.substring(10);
    if (v.length > 13) v = v.substring(0, 15) + '-' + v.substring(15);
    return v;
  }
}
