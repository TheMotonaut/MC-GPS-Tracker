
import React, {
  useState,
  useEffect,
} from 'react';
import LoadingScreen from '../screens/loading_screen';

const context = {};

export const LanguageContext = React.createContext(
  context
);

export interface LanguageProviderProps {
  children: React.ReactNode;
}

function LanguageProvider(props: LanguageProviderProps): JSX.Element {
  const [getStateIsLoading, setStateIsLoading] = useState(true);
  const [getStateTranslations, setStateTranslations] = useState(
    {
      vehicleCard: {
        status: 'status',
      }
    }
  );
  const {
    children,
  } = props;
  useEffect(() => {
    setStateIsLoading(false);
  }, []);
  return (
    <LanguageContext.Provider value={
      {
        translations: getStateTranslations,
      }
    }>
      {getStateIsLoading ? <LoadingScreen /> : children}
    </LanguageContext.Provider>
  );
}

export default LanguageProvider;
