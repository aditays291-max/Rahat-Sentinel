export interface HazardMetadata {
    label: string;
    description: string;
    priority: number; // Higher value = higher priority
    color: string; // Semantic color name (e.g., 'red', 'orange', 'yellow', 'green')
}
