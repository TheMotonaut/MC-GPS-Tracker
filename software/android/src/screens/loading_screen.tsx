
import React, {
  useContext,
} from 'react';
import {
  StyleSheet,
  View,
  Text,
  Image,
} from 'react-native';
import {
  LanguageContext,
} from '../context/language_context';

export interface LoadingScreenProps {
  children: React.ReactNode;
}

function LoadingScreen(props: LoadingScreenProps): JSX.Element {
  const {
    translations,
  } = useContext(LanguageContext);
  return (
    <View style={styles.container}>
      <Image
        style={styles.img}
        source={require('../assets/images/uni.png')}
       />
      <Text style={styles.text}>
        {"Loading"}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create(
  {
    container: {
      flex: 1,
      backgroundColor: '#000',
      justifyContent: 'center',
      alignItems: 'center',
    },
    img: {
      width: 52,
      height: 52,
      marginBottom: 10,
      flex: 0,
    },
    text: {
      color: '#FFF',
      flex: 0,
    }
  }
);

export default LoadingScreen;
