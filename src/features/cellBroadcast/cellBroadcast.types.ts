export interface RawCellBroadcastMessage {
  messageId: string;
  title: string;
  body: string;
  severity: string | number;
  latitude?: number;
  longitude?: number;
  radius?: number;
  timestamp: number;
}
