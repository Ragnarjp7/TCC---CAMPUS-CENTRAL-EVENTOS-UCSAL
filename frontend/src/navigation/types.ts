import type { BottomTabScreenProps } from "@react-navigation/bottom-tabs";
import type { CompositeScreenProps, NavigatorScreenParams } from "@react-navigation/native";
import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import type { CampusEvent } from "@/features/events/types";

export type TabParamList = {
  Início: undefined;
  Perfil: undefined;
  Notificações: undefined;
};

export type RootStackParamList = {
  Splash: undefined;
  Login: undefined;
  Onboarding: undefined;
  Main: NavigatorScreenParams<TabParamList> | undefined;
  /** `event` é opcional: serve só para exibir na hora enquanto a tela busca pelo id. */
  EventDetail: { eventId: number; event?: CampusEvent };
  Certificates: undefined;
  Menu: undefined;
  /** Sem `eventId`: nova proposta. Com `eventId`: reenvio após a CAEX pedir ajustes. */
  CreateEvent: { eventId?: number } | undefined;
  /** RF27 - Eventos do usuário e a situação de cada um na análise da CAEX. */
  MyEvents: undefined;
  /** RF29 - Fila de análise da CAEX. */
  ReviewQueue: undefined;
  /** RF29 - Análise de um evento pela CAEX. */
  ReviewEvent: { eventId: number };
  Placeholder: { title: string };
};

export type RootScreenProps<T extends keyof RootStackParamList> = NativeStackScreenProps<RootStackParamList, T>;

export type TabScreenProps<T extends keyof TabParamList> = CompositeScreenProps<
  BottomTabScreenProps<TabParamList, T>,
  NativeStackScreenProps<RootStackParamList>
>;

declare global {
  namespace ReactNavigation {
    interface RootParamList extends RootStackParamList {}
  }
}
