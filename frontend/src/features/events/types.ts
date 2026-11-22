export type EventMode = "Presenciais" | "Online";

export type EventMonth = "NOV" | "DEZ";

export interface CampusEvent {
  id: number;
  day: string;
  month: EventMonth;
  title: string;
  place: string;
  mode: EventMode;
  organizer: string;
  time: string;
}
