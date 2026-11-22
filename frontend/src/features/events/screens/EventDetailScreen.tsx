import type { ReactNode } from "react";
import { Pressable, ScrollView, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { Calendar, ChevronLeft, Clock, MapPin } from "lucide-react-native";
import type { LucideIcon } from "lucide-react-native";
import { useCan } from "@/features/auth/hooks/useCan";
import { Button, ImgSlot } from "@/shared/components/ui";
import { colors } from "@/shared/theme/colors";
import type { RootScreenProps } from "@/navigation/types";

interface InfoRowProps {
  Icon: LucideIcon;
  children: ReactNode;
  testID: string;
}

const InfoRow = ({ Icon, children, testID }: InfoRowProps) => (
  <View testID={testID} className="flex-row items-center gap-3">
    <View className="size-8 items-center justify-center rounded-lg bg-brand-soft">
      <Icon size={16} color={colors.brand} />
    </View>
    <Text testID={`${testID}-text`} className="text-sm text-slate-600">{children}</Text>
  </View>
);

export default function EventDetailScreen({ navigation, route }: RootScreenProps<"EventDetail">) {
  const { event } = route.params;
  const insets = useSafeAreaInsets();
  const canEnroll = useCan("event:enroll");

  return (
    <View testID="event-detail-screen" className="flex-1 bg-white">
      <ScrollView testID="event-detail-scroll" contentContainerClassName="pb-4">
        <View>
          <ImgSlot testID="event-detail-cover" className="h-56 w-full rounded-none" />
          <Pressable
            testID="event-detail-back-button"
            onPress={() => navigation.goBack()}
            accessibilityLabel="Voltar"
            style={{ top: insets.top + 8 }}
            className="absolute left-4 size-9 items-center justify-center rounded-full bg-white/90 shadow"
          >
            <ChevronLeft size={20} color={colors.slate800} />
          </Pressable>
        </View>

        <View className="-mt-4 rounded-t-3xl bg-white px-5 pt-6">
          <Text testID="event-detail-title" className="text-xl font-extrabold text-slate-900">{event.title}</Text>
          <View testID="event-detail-info" className="mt-4 gap-3">
            <InfoRow testID="event-detail-date" Icon={Calendar}>
              {event.day} de {event.month === "DEZ" ? "dezembro" : "novembro"} de 2026
            </InfoRow>
            <InfoRow testID="event-detail-time" Icon={Clock}>{event.time}</InfoRow>
            <InfoRow testID="event-detail-place" Icon={MapPin}>{event.place}</InfoRow>
          </View>

          <Text testID="event-detail-about-title" className="mt-6 text-sm font-bold text-slate-900">Sobre o evento</Text>
          <Text testID="event-detail-about-text" className="mt-1.5 text-sm leading-relaxed text-slate-500">
            Uma semana repleta de palestras, workshops e atividades para toda a comunidade acadêmica da UCSAL.
          </Text>

          <View testID="event-detail-organizer" className="mt-5 flex-row items-center gap-3 rounded-xl bg-slate-50 p-3">
            <ImgSlot testID="event-detail-organizer-avatar" className="size-10 rounded-full" />
            <View>
              <Text className="text-[11px] text-slate-400">Organizado por</Text>
              <Text testID="event-detail-organizer-name" className="text-sm font-semibold text-slate-800">{event.organizer}</Text>
            </View>
          </View>
        </View>
      </ScrollView>

      <View testID="event-detail-footer" style={{ paddingBottom: Math.max(insets.bottom, 16) }} className="bg-white px-5 pt-3">
        {canEnroll ? (
          <Button testID="event-detail-enroll-button" title="Inscrever-se" />
        ) : (
          <Button
            testID="event-detail-signin-button"
            title="Entrar para se inscrever"
            onPress={() => navigation.navigate("Login")}
          />
        )}
      </View>
    </View>
  );
}
