import { RefreshControl, ScrollView, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { StateView } from "@/shared/components/StateView";
import { useAuth } from "@/features/auth/context/AuthContext";
import type { RootScreenProps } from "@/navigation/types";
import { CertificateTile } from "../components/CertificateTile";
import { useCertificates } from "../hooks/useCertificates";
import type { Certificate } from "../types";

/** Agrupa os certificados em linhas de 2 para a grade de quadrados. */
const toRows = (items: Certificate[]): Certificate[][] => {
  const rows: Certificate[][] = [];
  for (let i = 0; i < items.length; i += 2) rows.push(items.slice(i, i + 2));
  return rows;
};

export default function CertificatesScreen({ navigation }: RootScreenProps<"Certificates">) {
  const { user, isRestoring } = useAuth();
  const isAuthenticated = user !== null;
  const feed = useCertificates(isAuthenticated && !isRestoring);

  // TODO: trocar pela tela de visualização/download do certificado
  const open = (c: Certificate) => navigation.navigate("Placeholder", { title: c.nome });

  return (
    <SafeAreaView testID="certificates-screen" edges={["bottom"]} className="flex-1 bg-slate-50">
      {!isAuthenticated && !isRestoring ? (
        <View testID="certificates-signed-out" className="flex-1 items-center px-8 pt-10">
          <Text className="text-center text-sm text-slate-400">Entre na sua conta para ver seus certificados.</Text>
        </View>
      ) : feed.viewState === "ready" ? (
        <ScrollView
          testID="certificates-grid"
          contentContainerClassName="gap-3 p-4 pb-10"
          refreshControl={<RefreshControl refreshing={feed.isRefetching} onRefresh={feed.retry} />}
        >
          {toRows(feed.certificates).map((row, r) => (
            <View key={r} testID={`certificates-row-${r}`} className="flex-row gap-3">
              {row.map((item) => (
                <CertificateTile key={item.id} certificate={item} onPress={() => open(item)} />
              ))}
              {/* mantém o último quadrado com a mesma largura quando a quantidade é ímpar */}
              {row.length === 1 && <View className="flex-1" />}
            </View>
          ))}
        </ScrollView>
      ) : (
        <StateView
          testID="certificates-state-view"
          state={feed.viewState}
          errorMessage={feed.errorMessage}
          emptyMessage="Você ainda não tem certificados."
          onRetry={feed.retry}
        />
      )}
    </SafeAreaView>
  );
}
