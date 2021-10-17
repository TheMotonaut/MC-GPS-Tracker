
import React from 'react';

import PreferenceProvider from './src/context/preference_context';
import LanguageProvider from './src/context/language_context';
import DataProvider from './src/context/data_context';
import UserProvider from './src/context/user_context';
import MainScreen from './src/screens/main_screen';

import translations from './src/languages/en';

const preferences = {}
const language = {}
const data = {}
const user = {}

function App(): JSX.Element {
  // useColorScheme() === 'dark'
  return (
    <PreferenceProvider>
      <LanguageProvider>
        <UserProvider>
          <DataProvider>
            <MainScreen />
          </DataProvider>
        </UserProvider>
      </LanguageProvider>
    </PreferenceProvider>
  );
};

export default App;
