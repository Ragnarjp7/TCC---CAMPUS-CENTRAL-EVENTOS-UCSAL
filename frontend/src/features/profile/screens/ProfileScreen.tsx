import { useMemo, useState } from "react";
import type { ReactElement } from "react";
import { FlatList, RefreshControl, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Button } from "@/shared/components/ui";
import { StateView } from "@/shared/components/StateView";
import type { ViewState } from "@/shared/state/viewState";
import type { TabScreenProps } from "@/navigation/types";
import { useAuth } from "@/features/auth/context/AuthContext";
import { getRoleLabel } from "@/features/auth/roles";
import { PostCard } from "@/features/feed/components/PostCard";
import { EnrollmentCard } from "../components/EnrollmentCard";
import { MemberCard } from "../components/MemberCard";
import { ProfileHeader, type ProfileTab } from "../components/ProfileHeader";
import { useGroupMembers, useGroupPosts, useMyEnrollments } from "../hooks/useProfile";

interface ListState<T> {
  items: T[];
  viewState: ViewState;
  errorMessage?: string;
  retry: () => void;
  isRefetching: boolean;
}

interface TabListProps<T extends { id: number }> {
  testID: string;
  state: ListState<T>;
  emptyMessage: string;
  renderItem: (item: T) => ReactElement;
}

/** Lista de uma aba com os estados de carregando/erro/vazio e "puxar para atualizar". */
function TabList<T extends { id: number }>({ testID, state, emptyMessage, renderItem }: TabListProps<T>) {
  if (state.viewState !== "ready") {
    return (
      <StateView
        testID={`${testID}-state-view`}
        state={state.viewState}
        errorMessage={state.errorMessage}
        emptyMessage={emptyMessage}
        onRetry={state.retry}
      />
    );
  }
  return (
    <FlatList
      testID={testID}
      data={state.items}
      keyExtractor={(item) => String(item.id)}
      renderItem={({ item }) => renderItem(item)}
      contentContainerClassName="gap-3 px-4 pt-4 pb-[110px]"
      refreshControl={<RefreshControl refreshing={state.isRefetching} onRefresh={state.retry} />}
    />
  );
}

const NO_GROUP_MESSAGE = "Você ainda não faz parte de nenhuma liga, empresa júnior ou atlética.";

export default function ProfileScreen({ navigation }: TabScreenProps<"Perfil">) {
  const { user, isRestoring } = useAuth();
  const [tab, setTab] = useState<ProfileTab>("Posts");

  const groupIds = useMemo(() => user?.leagues.map((l) => l.leagueId) ?? [], [user]);
  const hasGroups = groupIds.length > 0;
  const ready = user !== null && !isRestoring;

  // cada aba só busca na primeira vez em que é aberta; depois o cache do React Query mantém os dados
  const posts = useGroupPosts(groupIds, ready && hasGroups && tab === "Posts");
  const members = useGroupMembers(groupIds, ready && hasGroups && tab === "Membros");
  const enrollments = useMyEnrollments(ready && tab === "Eventos");

  if (!user) {
    return (
      <SafeAreaView testID="profile-screen" edges={["top"]} className="flex-1 bg-slate-50">
        <View testID="profile-content" className="flex-1 items-center justify-center gap-4 px-8">
          <Text testID="profile-guest-message" className="text-center text-sm text-slate-500">
            Entre para acessar conteúdos exclusivos da comunidade acadêmica.
          </Text>
          <Button testID="profile-signin-button" title="Entrar" onPress={() => navigation.navigate("Login")} />
        </View>
      </SafeAreaView>
    );
  }

  const noGroups = (
    <View testID="profile-no-groups" className="flex-1 items-center px-8 pt-10">
      <Text className="text-center text-sm text-slate-400">{NO_GROUP_MESSAGE}</Text>
    </View>
  );

  return (
    <View testID="profile-screen" className="flex-1 bg-slate-100">
      <ProfileHeader
        name={user.name}
        handle={user.email.split("@")[0]}
        subtitle={getRoleLabel(user)}
        tab={tab}
        onTabChange={setTab}
        onMenuPress={() => navigation.navigate("Menu")}
      />

      {tab === "Posts" &&
        (hasGroups ? (
          <TabList
            testID="profile-posts"
            state={posts}
            emptyMessage="Seus grupos ainda não publicaram nada."
            renderItem={(post) => (
              <PostCard
                post={post}
                onPress={post.eventId != null ? () => navigation.navigate("EventDetail", { eventId: post.eventId! }) : undefined}
              />
            )}
          />
        ) : (
          noGroups
        ))}

      {tab === "Membros" &&
        (hasGroups ? (
          <TabList
            testID="profile-members"
            state={members}
            emptyMessage="Nenhum membro encontrado."
            renderItem={(member) => <MemberCard member={member} showGroup={groupIds.length > 1} />}
          />
        ) : (
          noGroups
        ))}

      {tab === "Eventos" && (
        <TabList
          testID="profile-events"
          state={enrollments}
          emptyMessage="Você ainda não se inscreveu em nenhum evento."
          renderItem={(enrollment) => (
            <EnrollmentCard
              enrollment={enrollment}
              onPressDetails={() => navigation.navigate("EventDetail", { eventId: enrollment.event.id, event: enrollment.event })}
              onPressCertificate={() => navigation.navigate("Certificates")}
            />
          )}
        />
      )}
    </View>
  );
}
