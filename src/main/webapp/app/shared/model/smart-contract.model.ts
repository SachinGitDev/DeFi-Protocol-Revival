export interface ISmartContract {
  id?: number;
  name?: string;
  githubUrl?: string | null;
  originalCode?: string;
  resurrectedCode?: string | null;
  isValidated?: boolean | null;
}

export const defaultValue: Readonly<ISmartContract> = {
  isValidated: false,
};
