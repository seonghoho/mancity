import create from 'zustand';
import { devtools, persist } from 'zustand/middleware';

// UserType 인터페이스 정의
interface UserType {
  id: number;
  nickName: string;
  email: string;
  height: number;
  weight: number;
  foot: string;
  image: string;
  player: boolean; // 'player' 속성을 boolean으로 명시
  setUser: (newData: Partial<UserType>) => void;
}

// 사용할 초기 상태 정의
const initialState: UserType = {
  id: 0,
  nickName: "",
  email: "",
  height: 0,
  weight: 0,
  foot: "",
  image: "",
  player: false, // 여기에서 초기화
  setUser: () => {}, // 초기 상태에서는 빈 함수로 정의
};

const useUserStore = create<UserType>(
  devtools(
    persist(
      (set) => ({
        ...initialState, // 초기 상태 사용
        setUser: (newData: Partial<UserType>) => set((state) => ({
          ...state,
          ...newData,
        })),
      }),
      {
        name: 'userStore', // localStorage에 저장될 이름
      }
    )
  )
);

export default useUserStore;
