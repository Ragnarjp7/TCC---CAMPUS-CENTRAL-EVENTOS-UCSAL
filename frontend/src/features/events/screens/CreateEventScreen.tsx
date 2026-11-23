import { useMemo, useRef, useState } from "react";
import type { ComponentRef, ReactNode } from "react";
import { ActivityIndicator, Alert, KeyboardAvoidingView, Platform, Pressable, ScrollView, Text, TextInput, View } from "react-native";
import type { TextInputProps } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Check, ChevronLeft, MessageSquareWarning } from "lucide-react-native";
import { colors } from "@/shared/theme/colors";
import type { RootScreenProps } from "@/navigation/types";
import { useAuth } from "@/features/auth/context/AuthContext";
import { isInstitutional, leaguesWith } from "@/features/auth/permissions";
import { getErrorMessage } from "@/shared/api/errors";
import { StateView } from "@/shared/components/StateView";
import {
  ABRANGENCIAS,
  APRESENTACAO_MAX,
  AREAS_TEMATICAS,
  ESPACOS,
  INVESTIMENTOS,
  LOCAIS,
  RECURSOS,
  TIPOS_EXTENSAO,
  TITULO_MAX,
  VAGAS,
  emptyProposal,
  maskCpf,
  maskCurrency,
  maskDate,
  maskPhone,
  maskTime,
  proposalFromSubmission,
  toEventoRequest,
  validateProposal,
} from "../proposal";
import type { EventProposal, ProposalErrors, ProposalField, YesNo } from "../proposal";
import { useEventCategories } from "../hooks/useEventCategories";
import { useSubmission, useSubmitEvent } from "../hooks/useSubmissions";
import type { EventSubmission } from "../types";

// ---------- Seções (usadas para rolar até o primeiro erro) ----------

type SectionId = "proponente" | "evento" | "certificacao" | "investimento" | "pecas" | "observacoes" | "lgpd";

const SECTION_ORDER: SectionId[] = ["proponente", "evento", "certificacao", "investimento", "pecas", "observacoes", "lgpd"];

const FIELD_SECTION: Partial<Record<ProposalField, SectionId>> = {
  certificado: "certificacao",
  cienteCertificado: "certificacao",
  outrosPapeis: "certificacao",
  investimento: "investimento",
  valor: "investimento",
  linkInscricao: "investimento",
  linkPecas: "pecas",
  observacoes: "observacoes",
  lgpd: "lgpd",
};

const PROPONENTE_FIELDS: ProposalField[] = [
  "nome", "cpf", "email", "telefone", "lattes", "miniCurriculo", "isEntidade", "ligaId", "entidade",
  "presidenteNome", "presidenteEmail", "presidenteTelefone", "presidenteCpf",
];

const sectionOf = (field: ProposalField): SectionId =>
  FIELD_SECTION[field] ?? (PROPONENTE_FIELDS.includes(field) ? "proponente" : "evento");

const YES_NO: { value: YesNo; label: string }[] = [
  { value: "sim", label: "Sim" },
  { value: "nao", label: "Não" },
];

const asOptions = (list: readonly string[]) => list.map((v) => ({ value: v, label: v }));

// ---------- Componentes do formulário ----------

const Label = ({ text, required }: { text: string; required?: boolean }) => (
  <Text className="text-xs font-semibold text-slate-400">
    {text}
    {required && <Text className="text-red-500"> *</Text>}
  </Text>
);

const ErrorText = ({ message }: { message?: string }) =>
  message ? <Text className="text-[11px] font-medium text-red-500">{message}</Text> : null;

interface SectionProps {
  id: SectionId;
  number: number;
  title: string;
  children: ReactNode;
  onMeasure: (id: SectionId, y: number) => void;
}

const Section = ({ id, number, title, children, onMeasure }: SectionProps) => (
  <View
    testID={`proposal-section-${id}`}
    onLayout={(e) => onMeasure(id, e.nativeEvent.layout.y)}
    className="gap-4 rounded-2xl border border-slate-100 bg-white p-4"
  >
    <View className="flex-row items-center gap-2.5">
      <View className="size-7 items-center justify-center rounded-full bg-brand-soft">
        <Text className="text-xs font-extrabold text-brand">{number}</Text>
      </View>
      <Text className="flex-1 text-sm font-extrabold uppercase tracking-wide text-slate-500">{title}</Text>
    </View>
    {children}
  </View>
);

interface FormFieldProps extends TextInputProps {
  label: string;
  required?: boolean;
  error?: string;
  hint?: string;
  className?: string;
  testID: string;
}

const FormField = ({ label, required, error, hint, className = "", testID, multiline, ...props }: FormFieldProps) => (
  <View className={`gap-1.5 ${className}`}>
    <Label text={label} required={required} />
    <TextInput
      testID={testID}
      placeholderTextColor={colors.slate300}
      multiline={multiline}
      textAlignVertical={multiline ? "top" : "center"}
      className={`w-full rounded-xl border bg-slate-50 px-3.5 text-sm text-slate-500 focus:border-brand ${
        error ? "border-red-400" : "border-slate-200"
      } ${multiline ? "min-h-24 py-3" : "h-11"}`}
      {...props}
    />
    {error ? <ErrorText message={error} /> : hint ? <Text className="text-[11px] text-slate-400">{hint}</Text> : null}
  </View>
);

interface OptionGroupProps<T extends string> {
  label: string;
  required?: boolean;
  error?: string;
  options: { value: T; label: string }[];
  testID: string;
  /** "row" para poucas opções curtas (Sim/Não), "list" para opções longas. */
  layout?: "row" | "list";
  multiple?: boolean;
  value: T | null | T[];
  onChange: (value: T) => void;
}

function OptionGroup<T extends string>({
  label,
  required,
  error,
  options,
  testID,
  layout = "list",
  multiple = false,
  value,
  onChange,
}: OptionGroupProps<T>) {
  const isSelected = (v: T) => (Array.isArray(value) ? value.includes(v) : value === v);

  return (
    <View className="gap-2" testID={testID}>
      <Label text={label} required={required} />
      <View className={layout === "row" ? "flex-row flex-wrap gap-2" : "gap-2"}>
        {options.map((opt) => {
          const selected = isSelected(opt.value);
          return (
            <Pressable
              key={opt.value}
              testID={`${testID}-${opt.value}`}
              accessibilityRole={multiple ? "checkbox" : "radio"}
              accessibilityState={multiple ? { checked: selected } : { selected }}
              onPress={() => onChange(opt.value)}
              className={`flex-row items-center gap-2.5 rounded-xl border px-3.5 py-3 active:opacity-80 ${
                layout === "row" ? "min-w-24 flex-1" : ""
              } ${selected ? "border-brand bg-brand-soft" : error ? "border-red-300 bg-slate-50" : "border-slate-200 bg-slate-50"}`}
            >
              <View
                className={`size-5 items-center justify-center border-2 ${multiple ? "rounded-md" : "rounded-full"} ${
                  selected ? "border-brand bg-brand" : "border-slate-300 bg-white"
                }`}
              >
                {selected && (multiple ? <Check size={12} color={colors.white} strokeWidth={3} /> : <View className="size-2 rounded-full bg-white" />)}
              </View>
              <Text className={`flex-1 text-sm ${selected ? "font-semibold text-brand" : "text-slate-500"}`}>{opt.label}</Text>
            </Pressable>
          );
        })}
      </View>
      <ErrorText message={error} />
    </View>
  );
}

interface AgreeBoxProps {
  label: string;
  checked: boolean;
  error?: string;
  testID: string;
  onToggle: () => void;
}

const AgreeBox = ({ label, checked, error, testID, onToggle }: AgreeBoxProps) => (
  <View className="gap-1.5">
    <Pressable
      testID={testID}
      accessibilityRole="checkbox"
      accessibilityState={{ checked }}
      onPress={onToggle}
      className={`flex-row items-start gap-3 rounded-xl border p-3.5 active:opacity-80 ${
        checked ? "border-brand bg-brand-soft" : error ? "border-red-300 bg-slate-50" : "border-slate-200 bg-slate-50"
      }`}
    >
      <View
        className={`mt-0.5 size-5 items-center justify-center rounded-md border-2 ${
          checked ? "border-brand bg-brand" : "border-slate-300 bg-white"
        }`}
      >
        {checked && <Check size={12} color={colors.white} strokeWidth={3} />}
      </View>
      <View className="flex-1 gap-1">
        <Text className="text-sm text-slate-500">
          {label}
          <Text className="text-red-500"> *</Text>
        </Text>
        <Text className={`text-xs font-semibold ${checked ? "text-brand" : "text-slate-400"}`}>Ciente</Text>
      </View>
    </Pressable>
    <ErrorText message={error} />
  </View>
);

// ---------- Tela ----------

const Header = ({ title, onBack }: { title: string; onBack: () => void }) => (
  <View className="flex-row items-center gap-3 px-5 py-3.5">
    <Pressable
      testID="create-event-back-button"
      accessibilityRole="button"
      accessibilityLabel="Voltar"
      onPress={onBack}
      className="size-9 items-center justify-center rounded-full bg-brand active:opacity-80"
    >
      <ChevronLeft size={20} color={colors.white} strokeWidth={2.5} />
    </Pressable>
    <Text className="text-lg font-extrabold text-slate-500">{title}</Text>
  </View>
);

/**
 * RF27 - Nova proposta, ou reenvio após a CAEX pedir ajustes (`eventId` na rota).
 * No reenvio, espera carregar o evento para montar o formulário já preenchido.
 */
export default function CreateEventScreen({ navigation, route }: RootScreenProps<"CreateEvent">) {
  const eventId = route.params?.eventId ?? null;
  const query = useSubmission(eventId);

  if (eventId == null) return <ProposalForm navigation={navigation} editing={null} />;

  if (!query.data) {
    return (
      <SafeAreaView testID="create-event-screen" edges={["top", "bottom"]} className="flex-1 bg-white">
        <Header title="Ajustar proposta" onBack={() => navigation.goBack()} />
        <StateView
          testID="create-event-state-view"
          state={query.isError ? "error" : "loading"}
          errorMessage={getErrorMessage(query.error)}
          emptyMessage=""
          onRetry={() => void query.refetch()}
        />
      </SafeAreaView>
    );
  }

  return <ProposalForm navigation={navigation} editing={query.data} />;
}

interface ProposalFormProps {
  navigation: RootScreenProps<"CreateEvent">["navigation"];
  /** Evento sendo reenviado após ajustes; null = nova proposta. */
  editing: EventSubmission | null;
}

function ProposalForm({ navigation, editing }: ProposalFormProps) {
  const { user } = useAuth();
  const categories = useEventCategories();
  const submitMutation = useSubmitEvent(editing?.id ?? null);

  // RF27: estudante só cria pela liga em que preside (cargo com CRIAR_EVENTO);
  // professor e coordenador podem criar sem liga.
  const institutional = isInstitutional(user);
  const leagues = leaguesWith(user, "CRIAR_EVENTO");
  const requiresLeague = !institutional;
  // No reenvio a liga do evento não muda
  const leagueLocked = editing !== null;

  const [form, setForm] = useState<EventProposal>(() => {
    const prefill: Partial<EventProposal> = { nome: user?.name ?? "", email: user?.email ?? "" };
    if (editing) return proposalFromSubmission(editing, prefill);
    if (requiresLeague) {
      // Evento de estudante é sempre de uma entidade (a liga); com uma liga só, já vem escolhida
      const only = leagues.length === 1 ? leagues[0] : null;
      Object.assign(prefill, { isEntidade: "sim", ligaId: only?.leagueId ?? null, entidade: only?.leagueName ?? "" });
    }
    return emptyProposal(prefill);
  });
  const [submitted, setSubmitted] = useState(false);
  const scrollRef = useRef<ComponentRef<typeof ScrollView>>(null);
  const sectionY = useRef<Partial<Record<SectionId, number>>>({});

  const mustPickLeague = requiresLeague && !leagueLocked;
  const rules = useMemo(() => ({ requiresLeague: mustPickLeague }), [mustPickLeague]);
  const errors: ProposalErrors = useMemo(
    () => (submitted ? validateProposal(form, rules) : {}),
    [submitted, form, rules],
  );

  const set = <K extends ProposalField>(field: K, value: EventProposal[K]) =>
    setForm((prev) => ({ ...prev, [field]: value }));

  /** Liga um campo de texto ao estado, com máscara opcional. */
  const bind = (field: ProposalField, mask?: (v: string) => string) => ({
    value: form[field] as string,
    error: errors[field],
    onChangeText: (v: string) => set(field, (mask ? mask(v) : v) as never),
  });

  const toggleRecurso = (r: string) =>
    set("recursos", form.recursos.includes(r) ? form.recursos.filter((x) => x !== r) : [...form.recursos, r]);

  const selectLeague = (id: string) => {
    const league = leagues.find((l) => String(l.leagueId) === id);
    if (league) setForm((prev) => ({ ...prev, ligaId: league.leagueId, entidade: league.leagueName }));
  };

  const setIsEntidade = (v: YesNo) =>
    // ao dizer que não é de entidade, o evento deixa de ser vinculado a uma liga
    setForm((prev) => ({ ...prev, isEntidade: v, ...(v === "nao" && { ligaId: null }) }));

  const measure = (id: SectionId, y: number) => {
    sectionY.current[id] = y;
  };

  const leave = () => (editing ? navigation.goBack() : navigation.replace("MyEvents"));

  const submit = async () => {
    if (submitMutation.isPending) return;
    setSubmitted(true);
    const found = validateProposal(form, rules);
    const fields = Object.keys(found) as ProposalField[];

    if (fields.length > 0) {
      const firstSection = SECTION_ORDER.find((s) => fields.some((f) => sectionOf(f) === s));
      const y = firstSection ? sectionY.current[firstSection] : undefined;
      if (y !== undefined) scrollRef.current?.scrollTo({ y: Math.max(0, y - 12), animated: true });
      Alert.alert(
        "Revise a proposta",
        fields.length === 1 ? "Há 1 campo pendente ou inválido." : `Há ${fields.length} campos pendentes ou inválidos.`,
      );
      return;
    }

    try {
      await submitMutation.mutateAsync(toEventoRequest(form));
    } catch (error) {
      Alert.alert(editing ? "Não foi possível reenviar" : "Não foi possível enviar", getErrorMessage(error));
      return;
    }

    Alert.alert(
      editing ? "Proposta reenviada" : "Proposta enviada",
      `"${form.titulo.trim()}" foi ${editing ? "reenviada" : "submetida"} à avaliação da CAEX. Você será notificado sobre o resultado.`,
      [{ text: "OK", onPress: leave }],
    );
  };

  const confirmLeave = () => {
    const touched = editing !== null || form.titulo.trim() !== "" || form.apresentacao.trim() !== "" || form.cpf !== "";
    if (!touched) return navigation.goBack();
    Alert.alert(editing ? "Descartar alterações?" : "Descartar proposta?", "Os dados preenchidos serão perdidos.", [
      { text: "Continuar editando", style: "cancel" },
      { text: "Descartar", style: "destructive", onPress: () => navigation.goBack() },
    ]);
  };

  const needsEspacoDetalhe = form.espaco === "Laboratório (especificar)" || form.espaco === "Outro";
  const leagueOptions = leagues.map((l) => ({ value: String(l.leagueId), label: l.leagueName }));
  const categoryOptions = categories.map((c) => ({ value: String(c.id), label: c.name }));

  return (
    <SafeAreaView testID="create-event-screen" edges={["top", "bottom"]} className="flex-1 bg-white">
      <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : undefined} className="flex-1">
        <Header title={editing ? "Ajustar proposta" : "Proposta de evento"} onBack={confirmLeave} />

        <ScrollView
          ref={scrollRef}
          className="bg-slate-50"
          contentContainerClassName="gap-4 px-4 pb-10 pt-4"
          keyboardShouldPersistTaps="handled"
        >
          <View className="gap-1 px-1">
            <Text className="text-sm text-slate-500">
              {editing
                ? "Faça os ajustes pedidos pela CAEX e reenvie a proposta para uma nova análise."
                : "Preencha os dados abaixo para submeter sua proposta à avaliação."}
            </Text>
            <Text className="text-xs text-slate-400">
              Campos marcados com <Text className="text-red-500">*</Text> são obrigatórios.
            </Text>
          </View>

          {editing?.caexNote && (
            <View testID="create-event-caex-note" className="flex-row gap-3 rounded-2xl border border-orange-200 bg-orange-50 p-4">
              <MessageSquareWarning size={18} color="#c2410c" />
              <View className="flex-1 gap-1">
                <Text className="text-xs font-bold uppercase tracking-wide text-orange-700">Ajustes pedidos pela CAEX</Text>
                <Text className="text-sm text-orange-800">{editing.caexNote}</Text>
              </View>
            </View>
          )}

          {/* 1. PROPONENTE */}
          <Section id="proponente" number={1} title="Informações sobre o proponente" onMeasure={measure}>
            <FormField testID="proposal-nome" label="Nome completo" required placeholder="Seu nome completo" autoComplete="name" {...bind("nome")} />
            <FormField testID="proposal-cpf" label="CPF" required placeholder="000.000.000-00" keyboardType="number-pad" {...bind("cpf", maskCpf)} />
            <FormField
              testID="proposal-email"
              label="E-mail"
              required
              placeholder="nome@ucsal.edu.br"
              keyboardType="email-address"
              autoCapitalize="none"
              autoComplete="email"
              {...bind("email")}
            />
            <FormField testID="proposal-telefone" label="Telefone para contato" required placeholder="(71) 90000-0000" keyboardType="phone-pad" {...bind("telefone", maskPhone)} />
            <FormField
              testID="proposal-lattes"
              label="Link do Lattes"
              required
              placeholder="http://lattes.cnpq.br/..."
              keyboardType="url"
              autoCapitalize="none"
              {...bind("lattes")}
            />
            <FormField testID="proposal-mini-curriculo" label="Mini currículo" required placeholder="Formação, atuação e experiências relevantes" multiline {...bind("miniCurriculo")} />

            {/* Estudante: o evento é sempre da liga que ele preside, então a pergunta não se aplica */}
            {!requiresLeague && (
              <OptionGroup
                testID="proposal-is-entidade"
                label="É um evento de alguma entidade estudantil? (Ligas, Atléticas, DAs, CAs, DCE)"
                required
                layout="row"
                options={YES_NO}
                value={form.isEntidade}
                error={errors.isEntidade}
                onChange={(v) => (leagueLocked ? undefined : setIsEntidade(v))}
              />
            )}

            {form.isEntidade === "sim" && (
              <View className="gap-4 rounded-xl border border-dashed border-slate-200 p-3.5">
                {leagueLocked ? (
                  <View testID="proposal-entidade-fixa" className="gap-1.5">
                    <Label text="Entidade responsável" />
                    <Text className="text-sm font-semibold text-slate-600">{editing?.leagueName ?? form.entidade}</Text>
                  </View>
                ) : requiresLeague ? (
                  leagueOptions.length > 0 ? (
                    <OptionGroup
                      testID="proposal-liga"
                      label="Liga responsável pelo evento"
                      required
                      options={leagueOptions}
                      value={form.ligaId != null ? String(form.ligaId) : null}
                      error={errors.ligaId}
                      onChange={selectLeague}
                    />
                  ) : (
                    <ErrorText message="Seu cargo não permite cadastrar eventos por nenhuma liga." />
                  )
                ) : (
                  <FormField
                    testID="proposal-entidade"
                    label="Qual entidade estudantil?"
                    required
                    placeholder="Ex.: Liga Acadêmica de ..."
                    {...bind("entidade")}
                  />
                )}
                <FormField testID="proposal-presidente-nome" label="Nome do presidente da entidade" placeholder="Nome completo" {...bind("presidenteNome")} />
                <FormField
                  testID="proposal-presidente-email"
                  label="E-mail do presidente"
                  placeholder="email@exemplo.com"
                  keyboardType="email-address"
                  autoCapitalize="none"
                  {...bind("presidenteEmail")}
                />
                <FormField
                  testID="proposal-presidente-telefone"
                  label="Telefone do presidente"
                  placeholder="(71) 90000-0000"
                  keyboardType="phone-pad"
                  {...bind("presidenteTelefone", maskPhone)}
                />
                <FormField
                  testID="proposal-presidente-cpf"
                  label="CPF do presidente"
                  placeholder="000.000.000-00"
                  keyboardType="number-pad"
                  {...bind("presidenteCpf", maskCpf)}
                />
              </View>
            )}
          </Section>

          {/* 2. EVENTO */}
          <Section id="evento" number={2} title="Informações sobre o evento" onMeasure={measure}>
            <FormField
              testID="proposal-titulo"
              label="2.1 - Título do evento"
              required
              placeholder="Digite o título do evento"
              maxLength={TITULO_MAX}
              {...bind("titulo")}
            />
            {categoryOptions.length > 0 ? (
              <OptionGroup
                testID="proposal-categoria"
                label="Categoria do evento"
                required
                layout="row"
                options={categoryOptions}
                value={form.categoriaId != null ? String(form.categoriaId) : null}
                error={errors.categoriaId}
                onChange={(v) => set("categoriaId", Number(v))}
              />
            ) : (
              <View className="gap-1.5">
                <Label text="Categoria do evento" required />
                <ErrorText message={errors.categoriaId ?? "Nenhuma categoria disponível. Tente novamente em instantes."} />
              </View>
            )}
            <FormField
              testID="proposal-caracterizacao"
              label="2.2 - Caracterização da Atividade de Extensão"
              required
              placeholder="Descreva a natureza da atividade de extensão"
              multiline
              {...bind("caracterizacao")}
            />
            <OptionGroup
              testID="proposal-area"
              label="2.3 - Área temática de extensão"
              required
              options={asOptions(AREAS_TEMATICAS)}
              value={form.area}
              error={errors.area}
              onChange={(v) => set("area", v)}
            />
            <OptionGroup
              testID="proposal-abrangencia"
              label="2.4 - Abrangência do evento"
              required
              layout="row"
              options={asOptions(ABRANGENCIAS)}
              value={form.abrangencia}
              error={errors.abrangencia}
              onChange={(v) => set("abrangencia", v)}
            />
            <OptionGroup
              testID="proposal-tipo-extensao"
              label="2.5 - Tipo de evento de Extensão"
              required
              options={asOptions(TIPOS_EXTENSAO)}
              value={form.tipoExtensao}
              error={errors.tipoExtensao}
              onChange={(v) => set("tipoExtensao", v)}
            />
            <FormField
              testID="proposal-apresentacao"
              label="2.6 - Apresentação do evento (breve descrição, até 10 linhas)"
              required
              placeholder="Apresente o evento em poucas linhas"
              multiline
              maxLength={APRESENTACAO_MAX}
              hint={`${form.apresentacao.length}/${APRESENTACAO_MAX} caracteres`}
              {...bind("apresentacao")}
            />
            <OptionGroup
              testID="proposal-publicar-site"
              label="2.7 - Gostaria de publicar esse texto no site?"
              required
              layout="row"
              options={YES_NO}
              value={form.publicarSite}
              error={errors.publicarSite}
              onChange={(v) => set("publicarSite", v)}
            />
            <FormField testID="proposal-objetivo-geral" label="2.8 - Objetivo Geral" required placeholder="Qual o principal objetivo do evento?" multiline {...bind("objetivoGeral")} />
            <FormField
              testID="proposal-objetivos-especificos"
              label="Objetivos Específicos"
              required
              placeholder="Liste os objetivos específicos"
              multiline
              {...bind("objetivosEspecificos")}
            />
            <FormField testID="proposal-publico-alvo" label="2.9 - Público Alvo" required placeholder="Ex.: Estudantes de Computação" multiline {...bind("publicoAlvo")} />

            <OptionGroup
              testID="proposal-local"
              label="2.10 - Local"
              required
              options={asOptions(LOCAIS)}
              value={form.local}
              error={errors.local}
              onChange={(v) => set("local", v)}
            />
            {form.local === "Outro" && (
              <FormField testID="proposal-local-outro" label="Especifique o local" required placeholder="Endereço ou nome do local" {...bind("localOutro")} />
            )}

            <OptionGroup
              testID="proposal-espaco"
              label="2.11 - Espaço para realização (preferencialmente)"
              required
              options={asOptions(ESPACOS)}
              value={form.espaco}
              error={errors.espaco}
              onChange={(v) => set("espaco", v)}
            />
            {needsEspacoDetalhe && (
              <FormField
                testID="proposal-espaco-detalhe"
                label={form.espaco === "Outro" ? "Especifique o espaço" : "Especifique o laboratório"}
                required
                placeholder={form.espaco === "Outro" ? "Descreva o espaço" : "Ex.: Laboratório de Redes"}
                {...bind("espacoDetalhe")}
              />
            )}
            <FormField
              testID="proposal-quantidade-espacos"
              label="2.12 - Especifique a quantidade (salas, laboratórios, etc.)"
              placeholder="Ex.: 2 salas de aula"
              {...bind("quantidadeEspacos")}
            />

            <OptionGroup
              testID="proposal-vagas"
              label="2.13 - Vagas (quantitativo esperado de participantes)"
              required
              options={asOptions(VAGAS)}
              value={form.vagas}
              error={errors.vagas}
              onChange={(v) => set("vagas", v)}
            />

            <OptionGroup
              testID="proposal-recursos"
              label="2.14 - Recursos Materiais e Audiovisuais"
              required
              multiple
              layout="row"
              options={asOptions(RECURSOS)}
              value={form.recursos}
              error={errors.recursos}
              onChange={toggleRecurso}
            />
            {form.recursos.includes("Outro") && (
              <FormField testID="proposal-recursos-outro" label="Quais outros recursos?" required placeholder="Ex.: Quadro branco, extensão" {...bind("recursosOutro")} />
            )}

            <View className="flex-row gap-3">
              <FormField
                testID="proposal-inicio"
                className="flex-1"
                label="2.15 - Início do evento"
                required
                placeholder="dd/mm/aaaa"
                keyboardType="number-pad"
                {...bind("inicio", maskDate)}
              />
              <FormField
                testID="proposal-hora-inicio"
                className="w-28"
                label="Horário"
                required
                placeholder="hh:mm"
                keyboardType="number-pad"
                {...bind("horaInicio", maskTime)}
              />
            </View>
            <View className="flex-row gap-3">
              <FormField
                testID="proposal-termino"
                className="flex-1"
                label="Término do evento"
                required
                placeholder="dd/mm/aaaa"
                keyboardType="number-pad"
                {...bind("termino", maskDate)}
              />
              <FormField
                testID="proposal-hora-termino"
                className="w-28"
                label="Horário"
                required
                placeholder="hh:mm"
                keyboardType="number-pad"
                {...bind("horaTermino", maskTime)}
              />
            </View>
            <FormField
              testID="proposal-dias-horarios"
              label="2.16 - Dias e horários"
              required
              placeholder="Ex.: Dia 23/02 das 08h às 12h"
              multiline
              {...bind("diasHorarios")}
            />
            <FormField
              testID="proposal-programacao"
              label="2.17 - Programação (temas que serão trabalhados)"
              required
              placeholder="Descreva os temas e a sequência das atividades"
              multiline
              {...bind("programacao")}
            />
          </Section>

          {/* 3. CERTIFICAÇÃO */}
          <Section id="certificacao" number={3} title="Certificação" onMeasure={measure}>
            <OptionGroup
              testID="proposal-certificado"
              label="Será necessário emissão de certificado para ouvintes e palestrantes?"
              required
              layout="row"
              options={YES_NO}
              value={form.certificado}
              error={errors.certificado}
              onChange={(v) => set("certificado", v)}
            />
            <AgreeBox
              testID="proposal-ciente-certificado"
              label="3.1 - O certificado é emitido pela UCSal, por meio digital, para quem teve 75% de frequência."
              checked={form.cienteCertificado}
              error={errors.cienteCertificado}
              onToggle={() => set("cienteCertificado", !form.cienteCertificado)}
            />
            <FormField
              testID="proposal-outros-papeis"
              label="3.2 - Quais outros papéis serão certificados, além do ouvinte?"
              placeholder="Ex.: Palestrante, monitor, organizador"
              multiline
              {...bind("outrosPapeis")}
            />
          </Section>

          {/* 4. INVESTIMENTO */}
          <Section id="investimento" number={4} title="Investimento" onMeasure={measure}>
            <OptionGroup
              testID="proposal-investimento"
              label="4.1 - O evento será"
              required
              layout="row"
              options={asOptions(INVESTIMENTOS)}
              value={form.investimento}
              error={errors.investimento}
              onChange={(v) => set("investimento", v)}
            />
            {form.investimento === "Pago" && (
              <FormField
                testID="proposal-valor"
                label="4.2 - Qual valor será cobrado?"
                required
                placeholder="R$ 0,00"
                keyboardType="number-pad"
                {...bind("valor", maskCurrency)}
              />
            )}
            <FormField
              testID="proposal-link-inscricao"
              label="4.3 - Link para inscrição no evento"
              required
              placeholder="https://..."
              keyboardType="url"
              autoCapitalize="none"
              {...bind("linkInscricao")}
            />
          </Section>

          {/* 5. PEÇAS PUBLICITÁRIAS */}
          <Section id="pecas" number={5} title="Peças publicitárias" onMeasure={measure}>
            <FormField
              testID="proposal-link-pecas"
              label="5.1 - Link (drive) com as peças de divulgação"
              placeholder="https://drive.google.com/..."
              keyboardType="url"
              autoCapitalize="none"
              {...bind("linkPecas")}
            />
          </Section>

          {/* 6. OBSERVAÇÕES */}
          <Section id="observacoes" number={6} title="Outras observações relevantes" onMeasure={measure}>
            <FormField
              testID="proposal-observacoes"
              label="Observações relevantes sobre o evento"
              placeholder="Algo mais que a CAEX deva saber?"
              multiline
              {...bind("observacoes")}
            />
          </Section>

          {/* 7. LGPD */}
          <Section id="lgpd" number={7} title="Autorização LGPD" onMeasure={measure}>
            <AgreeBox
              testID="proposal-lgpd"
              label="Declaro que li e concordo com a coleta e o tratamento dos meus dados pessoais pela UCSal (CAEX), nos termos da LGPD (Lei nº 13.709/2018)."
              checked={form.lgpd}
              error={errors.lgpd}
              onToggle={() => set("lgpd", !form.lgpd)}
            />
          </Section>

          <Pressable
            testID="create-event-publish-button"
            accessibilityRole="button"
            accessibilityState={{ busy: submitMutation.isPending, disabled: submitMutation.isPending }}
            disabled={submitMutation.isPending}
            onPress={() => void submit()}
            className={`mt-2 h-12 w-full flex-row items-center justify-center gap-2 rounded-full bg-brand active:opacity-80 ${
              form.lgpd && !submitMutation.isPending ? "" : "opacity-60"
            }`}
          >
            {submitMutation.isPending && <ActivityIndicator size="small" color={colors.white} />}
            <Text className="text-sm font-semibold text-white">
              {submitMutation.isPending ? "Enviando..." : editing ? "Reenviar proposta" : "Enviar proposta"}
            </Text>
          </Pressable>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
