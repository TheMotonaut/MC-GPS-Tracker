
import React, {
  useState,
  useEffect,
} from 'react';
import LoadingScreen from '../screens/loading_screen';

enum ThemePreference {
  DARK_THEME = 'dark',
  LIGHT_THEME = 'light',
}

enum LanguagePreference {
  LANGUAGE_EN = 'en',
  LANGUAGE_SV = 'sv',
}

const context = {};

export const PreferenceContext = React.createContext(
  context
);

export interface PreferenceProviderProps {
  children: React.ReactNode;
}

function PreferenceProvider(props: PreferenceProviderProps): JSX.Element {
  const [getStateIsLoading, setStateIsLoading] = useState(true);
  const [getStateTheme, setStateTheme] = useState(ThemePreference.DARK_THEME);
  const [getStateLanguage, setStateLanguage] = useState(LanguagePreference.LANGUAGE_EN);
  const {
    children,
  } = props;
  useEffect(() => {
    setStateIsLoading(false);
  }, []);
  return (
    <PreferenceContext.Provider value={
      {
        theme: getStateTheme,
        language: getStateLanguage,
      }
    }>
      {getStateIsLoading ? <LoadingScreen /> : children}
    </PreferenceContext.Provider>
  );
}

export default PreferenceProvider;
